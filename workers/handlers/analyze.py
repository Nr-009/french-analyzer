from fastapi import APIRouter
from pydantic import BaseModel
from typing import Dict, List
from config_reader import config
from lexique_loader import load_lexique
import spacy
from collections import Counter, defaultdict

router = APIRouter()

nlp = spacy.load("fr_core_news_sm")
lexique_data = load_lexique()

class AnalyzeRequest(BaseModel):
    text: str

class TopWord(BaseModel):
    word: str
    lemma: str
    frequency_rank: int
    pos: str
    sentence: str
    dependency_label: str

class DependencyComplexity(BaseModel):
    avg_tree_depth: float
    complex_structures: int
    max_tree_depth: int

class Statistics(BaseModel):
    total_words: int
    unique_lemmas: int
    sentences: int
    avg_sentence_length: float
    pos_distribution: Dict[str, int]
    named_entities: Dict[str, List[str]]
    dependency_complexity: DependencyComplexity

class AnalyzeResponse(BaseModel):
    difficulty_score: float
    statistics: Statistics
    top_words: List[TopWord]

def get_tree_depth(token):
    depth = 0
    current = token
    while current.head != current:
        depth += 1
        current = current.head
    return depth

def calculate_difficulty_score(top_words: List[dict], total_words: int, avg_depth: float) -> float:
    if not top_words or total_words == 0:
        return 0.0
    
    avg_rank = sum(w["frequency_rank"] for w in top_words) / len(top_words)
    rank_score = min(avg_rank / 10000, 1.0)
    
    depth_score = min(avg_depth / 10, 1.0)
    
    difficulty = (rank_score * 0.7) + (depth_score * 0.3)
    return round(difficulty, 2)

@router.post("/analyze", response_model=AnalyzeResponse)
def analyze_text(payload: AnalyzeRequest):
    text = payload.text
    
    doc = nlp(text)
    
    total_words = sum(1 for token in doc if not token.is_punct and not token.is_space)
    unique_lemmas = len(set(token.lemma_ for token in doc if not token.is_punct and not token.is_space))
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
            if ent.label_ == "PER":
                entity_sets["PER"].add(ent.text)
            elif ent.label_ == "LOC":
                entity_sets["LOC"].add(ent.text)
            elif ent.label_ == "ORG":
                entity_sets["ORG"].add(ent.text)
            else:
                entity_sets["MISC"].add(ent.text)
        
        for key in named_entities:
            named_entities[key] = sorted(list(entity_sets[key]))
    
    tree_depths = []
    if config.INCLUDE_DEPENDENCY_ANALYSIS:
        for sent in sentences:
            for token in sent:
                if not token.is_punct and not token.is_space:
                    tree_depths.append(get_tree_depth(token))
    
    avg_tree_depth = round(sum(tree_depths) / len(tree_depths), 1) if tree_depths else 0.0
    max_tree_depth = max(tree_depths) if tree_depths else 0
    complex_structures = sum(1 for d in tree_depths if d > config.COMPLEX_SENTENCE_DEPTH)
    
    word_data = []
    sent_map = {}
    for sent in sentences:
        for token in sent:
            sent_map[token.i] = sent.text
    
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
    top_words = word_data[:config.TOP_WORDS_LIMIT]
    
    difficulty_score = calculate_difficulty_score(top_words, total_words, avg_tree_depth)
    
    return AnalyzeResponse(
        difficulty_score=difficulty_score,
        statistics=Statistics(
            total_words=total_words,
            unique_lemmas=unique_lemmas,
            sentences=num_sentences,
            avg_sentence_length=avg_sentence_length,
            pos_distribution=dict(pos_counter),
            named_entities=named_entities,
            dependency_complexity=DependencyComplexity(
                avg_tree_depth=avg_tree_depth,
                complex_structures=complex_structures,
                max_tree_depth=max_tree_depth
            )
        ),
        top_words=[TopWord(**word) for word in top_words]
    )