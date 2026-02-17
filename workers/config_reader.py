import os
from typing import List
from dotenv import load_dotenv

load_dotenv()

class ConfigReader:
    
    def __init__(self):
        self.TOP_WORDS_LIMIT = self._get_int("TOP_WORDS_LIMIT", 25)
        self.MIN_WORD_LENGTH = self._get_int("MIN_WORD_LENGTH", 3)
        self.INCLUDE_PROPER_NOUNS = self._get_bool("INCLUDE_PROPER_NOUNS", True)
        
        self.RARE_WORD_THRESHOLD = self._get_int("RARE_WORD_THRESHOLD", 5000)
        self.COMMON_WORD_THRESHOLD = self._get_int("COMMON_WORD_THRESHOLD", 1000)
        
        self.COMPLEX_SENTENCE_DEPTH = self._get_int("COMPLEX_SENTENCE_DEPTH", 5)
        self.MAX_CONTEXT_SENTENCE_LENGTH = self._get_int("MAX_CONTEXT_SENTENCE_LENGTH", 150)
        
        self.EXCLUDE_POS_TAGS = self._get_list("EXCLUDE_POS_TAGS", ["DET", "ADP", "PRON"])
        
        self.INCLUDE_POS_DISTRIBUTION = self._get_bool("INCLUDE_POS_DISTRIBUTION", True)
        self.INCLUDE_NAMED_ENTITIES = self._get_bool("INCLUDE_NAMED_ENTITIES", True)
        self.INCLUDE_DEPENDENCY_ANALYSIS = self._get_bool("INCLUDE_DEPENDENCY_ANALYSIS", True)
        
        self.PYTHON_NLP_PORT = self._get_int("PYTHON_NLP_PORT", 10001)
        self.LOG_LEVEL = os.getenv("LOG_LEVEL", "info")
    
    def _get_int(self, key: str, default: int) -> int:
        try:
            return int(os.getenv(key, str(default)))
        except ValueError:
            print(f"Warning: Invalid value for {key}, using default: {default}")
            return default
    
    def _get_bool(self, key: str, default: bool) -> bool:
        value = os.getenv(key, str(default)).lower()
        return value in ("true", "1", "yes", "on")
    
    def _get_list(self, key: str, default: List[str]) -> List[str]:
        value = os.getenv(key)
        if not value:
            return default
        return [item.strip() for item in value.split(",")]
    
    def display_config(self):
        print("=" * 60)
        print("FRENCH ANALYZER CONFIGURATION")
        print("=" * 60)
        print(f"TOP_WORDS_LIMIT: {self.TOP_WORDS_LIMIT}")
        print(f"MIN_WORD_LENGTH: {self.MIN_WORD_LENGTH}")
        print(f"INCLUDE_PROPER_NOUNS: {self.INCLUDE_PROPER_NOUNS}")
        print(f"RARE_WORD_THRESHOLD: {self.RARE_WORD_THRESHOLD}")
        print(f"COMMON_WORD_THRESHOLD: {self.COMMON_WORD_THRESHOLD}")
        print(f"COMPLEX_SENTENCE_DEPTH: {self.COMPLEX_SENTENCE_DEPTH}")
        print(f"MAX_CONTEXT_SENTENCE_LENGTH: {self.MAX_CONTEXT_SENTENCE_LENGTH}")
        print(f"EXCLUDE_POS_TAGS: {self.EXCLUDE_POS_TAGS}")
        print(f"INCLUDE_POS_DISTRIBUTION: {self.INCLUDE_POS_DISTRIBUTION}")
        print(f"INCLUDE_NAMED_ENTITIES: {self.INCLUDE_NAMED_ENTITIES}")
        print(f"INCLUDE_DEPENDENCY_ANALYSIS: {self.INCLUDE_DEPENDENCY_ANALYSIS}")
        print("=" * 60)

config = ConfigReader()