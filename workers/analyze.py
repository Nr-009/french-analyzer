import time
import spacy
from collections import Counter
from config_reader import config
from lexique_loader import load_lexique

nlp = spacy.load("fr_core_news_sm")
lexique_data = load_lexique()

class Analyzer:

    def analyze(self, text: str) -> dict:
        start_time = time.time()
        doc = nlp(text)

        total_words = sum(1 for t in doc if not t.is_punct and not t.is_space)
        unique_lemmas = len(set(t.lemma_ for t in doc if not t.is_punct and not t.is_space))
        sentences = list(doc.sents)
        num_sentences = len(sentences)
        avg_sentence_length = round(total_words / num_sentences, 1) if num_sentences > 0 else 0.0

        pos_counter = Counter()
        if config.INCLUDE_POS_DISTRIBUTION:
            for token in doc:
                if not token.is_space:
                    pos_counter[token.pos_] += 1

        named_entities = {"PER": [], "LOC": [], "ORG": [], "MISC": []}
        if config.INCLUDE_NAMED_ENTITIES:
            entity_sets = {"PER": set(), "LOC": set(), "ORG": set(), "MISC": set()}
            for ent in doc.ents:
                label = ent.label_ if ent.label_ in entity_sets else "MISC"
                entity_sets[label].add(ent.text)
            for key in named_entities:
                named_entities[key] = sorted(list(entity_sets[key]))

        tree_depths = []
        if config.INCLUDE_DEPENDENCY_ANALYSIS:
            for sent in sentences:
                for token in sent:
                    if not token.is_punct and not token.is_space:
                        tree_depths.append(self._get_tree_depth(token))

        avg_tree_depth = round(sum(tree_depths) / len(tree_depths), 1) if tree_depths else 0.0
        max_tree_depth = max(tree_depths) if tree_depths else 0
        complex_structures = sum(1 for d in tree_depths if d > config.COMPLEX_SENTENCE_DEPTH)

        sent_map = {}
        for sent in sentences:
            for token in sent:
                sent_map[token.i] = sent.text

        word_data = []
        for token in doc:
            if token.is_punct or token.is_space:
                continue
            if token.pos_ in config.EXCLUDE_POS_TAGS:
                continue
            if len(token.text) < config.MIN_WORD_LENGTH:
                continue
            if not config.INCLUDE_PROPER_NOUNS and token.pos_ == "PROPN":
                continue

            lemma = token.lemma_.lower()
            freq_rank = lexique_data.get(lemma, 999999)
            if freq_rank == 999999:
                continue

            sentence_text = sent_map.get(token.i, text)
            if len(sentence_text) > config.MAX_CONTEXT_SENTENCE_LENGTH:
                sentence_text = sentence_text[:config.MAX_CONTEXT_SENTENCE_LENGTH] + "..."

            word_data.append({
                "word": token.text,
                "lemma": token.lemma_,
                "frequency_rank": freq_rank,
                "pos": token.pos_,
                "sentence": sentence_text,
                "dependency_label": token.dep_
            })

        word_data.sort(key=lambda x: x["frequency_rank"], reverse=True)

        # Deduplicate by lemma, keep first occurrence (highest rank)
        seen_lemmas = set()
        unique_word_data = []
        for w in word_data:
            if w["lemma"].lower() not in seen_lemmas:
                seen_lemmas.add(w["lemma"].lower())
                unique_word_data.append(w)

        top_words = unique_word_data[:config.TOP_WORDS_LIMIT]
        difficulty_score = self._calculate_difficulty(top_words, total_words, avg_tree_depth)

        return {
            "difficulty_score": difficulty_score,
            "processing_time_ms": int((time.time() - start_time) * 1000),
            "statistics": {
                "total_words": total_words,
                "unique_lemmas": unique_lemmas,
                "sentences": num_sentences,
                "avg_sentence_length": avg_sentence_length,
                "pos_distribution": dict(pos_counter),
                "named_entities": named_entities,
                "dependency_complexity": {
                    "avg_tree_depth": avg_tree_depth,
                    "complex_structures": complex_structures,
                    "max_tree_depth": max_tree_depth
                }
            },
            "top_words": top_words
        }

    def _get_tree_depth(self, token) -> int:
        depth = 0
        current = token
        while current.head != current:
            depth += 1
            current = current.head
        return depth

    def _calculate_difficulty(self, top_words, total_words, avg_depth) -> float:
        if not top_words or total_words == 0:
            return 0.0
        avg_rank = sum(w["frequency_rank"] for w in top_words) / len(top_words)
        rank_score = min(avg_rank / 10000, 1.0)
        depth_score = min(avg_depth / 10, 1.0)
        return round((rank_score * 0.7) + (depth_score * 0.3), 2)

analyzer = Analyzer()