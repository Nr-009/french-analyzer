import pandas as pd
import os

def load_lexique():
    lexique_path = os.path.join(os.path.dirname(__file__), "data", "Lexique383.tsv")
    
    if not os.path.exists(lexique_path):
        print(f"WARNING: Lexique file not found at {lexique_path}")
        print("Download it from: http://www.lexique.org/databases/Lexique383/Lexique383.tsv")
        return {}
    
    print(f"Loading Lexique database from {lexique_path}...")
    
    df = pd.read_csv(lexique_path, sep='\t', encoding='utf-8')
    
    df = df[['lemme', 'freqlemfilms2']].copy()
    df = df[df['freqlemfilms2'] > 0]
    
    df = df.sort_values('freqlemfilms2', ascending=False)
    df['rank'] = range(1, len(df) + 1)
    
    lexique_dict = {}
    for _, row in df.iterrows():
        lemma = str(row['lemme']).lower().strip()
        rank = int(row['rank'])
        lexique_dict[lemma] = rank
    
    print(f"Loaded {len(lexique_dict)} lemmas with frequency rankings")
    
    return lexique_dict