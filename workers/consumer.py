import pika
import json
import requests
from analyze import analyzer
from config_reader import config

def callback(ch, method, properties, body):
    try:
        message = json.loads(body.decode())
        job_id = message["job_id"]
        filename = message["filename"]
        chunk_index = message["chunk_index"]
        chunk_name = message["chunk_name"]
        text = message["text"]

        print(f"Processing chunk {chunk_name} for {filename} (job: {job_id})")
        result = analyzer.analyze(text)

        result["job_id"] = job_id
        result["filename"] = filename
        result["chunk_index"] = chunk_index
        result["chunk_name"] = chunk_name

        requests.post(
            f"http://{config.SPRING_BOOT_HOST}:{config.SPRING_BOOT_PORT}/api/chunk-result",
            json=result
        )
        print(f"Result sent for chunk {chunk_name}, difficulty: {result['difficulty_score']}")
        ch.basic_ack(delivery_tag=method.delivery_tag)

    except Exception as e:
        print(f"Error: {e}")
        ch.basic_nack(delivery_tag=method.delivery_tag)

def main():
    credentials = pika.PlainCredentials(config.RABBITMQ_USER, config.RABBITMQ_PASS)
    connection = pika.BlockingConnection(
        pika.ConnectionParameters(host=config.RABBITMQ_HOST, credentials=credentials)
    )
    channel = connection.channel()
    channel.queue_declare(queue=config.QUEUE_NAME, durable=False)
    channel.basic_qos(prefetch_count=1)
    channel.basic_consume(queue=config.QUEUE_NAME, on_message_callback=callback)

    print("Worker ready, waiting for chunks...")
    channel.start_consuming()

if __name__ == "__main__":
    main()
