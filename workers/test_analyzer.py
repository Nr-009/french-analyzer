import requests
import json
import sys
import os

from config_reader import config

BASE_URL = f"http://127.0.0.1:{config.PYTHON_NLP_PORT}"

test_cases = [
    {
        "name": "Naruto - Entraînement",
        "text": """Naruto se tenait debout au sommet de la cascade, épuisé mais déterminé. Son maître Jiraya 
        lui avait donné une mission impossible: maîtriser le Rasengan en une semaine. Les autres ninjas 
        avaient mis des mois, voire des années, pour apprendre cette technique. Mais Naruto n'était pas 
        comme les autres. Il créa des centaines de clones pour accélérer l'entraînement. Chaque clone 
        travaillait sur une partie différente de la technique. Certains se concentraient sur la rotation 
        du chakra, d'autres sur la puissance, et d'autres encore sur la stabilisation. Kakashi observait 
        de loin, impressionné par cette méthode d'apprentissage ingénieuse."""
    },
    {
        "name": "One Piece - Rêve de pirate",
        "text": """Luffy regardait l'horizon depuis la proue du Thousand Sunny. Le vent marin ébouriffait ses 
        cheveux noirs tandis qu'il souriait largement. Depuis son départ du village de Fûsha, son rêve 
        n'avait jamais changé: devenir le Roi des Pirates. Ses nakama l'entouraient sur le pont. Zoro 
        s'entraînait avec ses trois sabres, Nami étudiait ses cartes marines, Sanji préparait le repas 
        dans la cuisine. Chacun avait son propre rêve, mais tous naviguaient ensemble vers Grand Line. 
        Les dangers étaient nombreux: la Marine, les autres pirates, les tempêtes imprévisibles. Mais 
        rien ne pourrait arrêter l'équipage du Chapeau de Paille."""
    },
    {
        "name": "Attack on Titan - Le Mur",
        "text": """Eren fixait le mur colossal qui protégeait l'humanité depuis cent ans. Cinquante mètres 
        de pierre s'élevaient vers le ciel, censés garder les Titans à l'extérieur. La vie à l'intérieur 
        des murs était paisible mais monotone. Les gens avaient oublié la peur, oublié la menace qui 
        rôdait au-delà des fortifications. Mais Eren, lui, rêvait de liberté. Il voulait voir l'océan, 
        explorer le monde extérieur. Ses amis Mikasa et Armin partageaient certaines de ses aspirations, 
        bien que plus prudemment. Ce jour-là, tout allait changer. Un éclair aveuglant fendit le ciel. 
        Le sol trembla violemment. Et soudain, une main gigantesque apparut au sommet du mur."""
    },
    {
        "name": "My Hero Academia - Examen d'entrée",
        "text": """Izuku Midoriya tremblait devant les portes de l'académie Yuei. Autour de lui, des dizaines 
        d'adolescents exhibaient fièrement leurs Alters. Certains créaient des flammes, d'autres 
        manipulaient la glace ou lévitaient dans les airs. Lui, jusqu'à récemment, n'avait aucun pouvoir. 
        Mais tout avait changé lors de sa rencontre avec All Might, le plus grand héros du monde. Il lui 
        avait transmis le One For All, un Alter qui se transmet de génération en génération. Maintenant, 
        Izuku devait prouver qu'il méritait cette chance. L'examen pratique consistait à détruire des 
        robots géants dans une ville factice. Points accordés selon le nombre de robots détruits."""
    },
    {
        "name": "Demon Slayer - Combat nocturne",
        "text": """Tanjiro dégaina son sabre noir dans l'obscurité de la forêt. Devant lui, le démon ricanait, 
        ses yeux rouges brillant dans la nuit. L'odeur du sang frais emplissait l'air. Nezuko, transformée 
        en démon mais toujours sa sœur, grondait à ses côtés. Tanjiro se concentra sur sa respiration. 
        La Danse du Dieu du Feu, la technique secrète de sa famille, coulait dans ses veines. Il visualisa 
        l'eau, ses mouvements fluides et puissants. Première forme: Surface d'eau tranchante! Son sabre 
        fendit l'air avec une précision mortelle. Le démon esquiva de justesse, surpris par la vitesse 
        du jeune pourfendeur. Le combat ne faisait que commencer."""
    },
    {
        "name": "Fullmetal Alchemist - Équivalence",
        "text": """Edward Elric traça un cercle de transmutation sur le sol poussiéreux. L'alchimie suivait 
        une loi fondamentale: l'échange équivalent. Pour obtenir quelque chose, il faut sacrifier quelque 
        chose de valeur égale. Lui et son frère Alphonse avaient appris cette leçon de la manière la plus 
        cruelle. Leur tentative de ressusciter leur mère décédée s'était terminée en catastrophe. Edward 
        avait perdu sa jambe gauche et son bras droit. Alphonse avait perdu son corps entier, son âme 
        maintenant liée à une armure. Depuis ce jour tragique, les deux frères cherchaient la Pierre 
        Philosophale, seul espoir de retrouver leurs corps originaux. Leur quête les menait à travers 
        tout le pays d'Amestris."""
    },
    {
        "name": "Sword Art Online - Monde virtuel",
        "text": """Kirito ouvrit les yeux dans le village de départ d'Aincrad. Autour de lui, dix mille joueurs 
        découvraient le monde fantastique de Sword Art Online. Les graphismes étaient époustouflants, 
        dépassant tout ce qu'il avait imaginé. Grâce au NerveGear, ses cinq sens étaient complètement 
        immergés dans la réalité virtuelle. Il pouvait sentir le vent sur son visage, entendre le 
        bruissement des feuilles, toucher les murs de pierre du château. Mais la joie initiale se 
        transforma rapidement en horreur. Le créateur du jeu, Kayaba Akihiko, apparut dans le ciel comme 
        un dieu géant. Il annonça la terrible vérité: impossible de se déconnecter. Mourir dans le jeu 
        signifiait mourir dans la réalité. Le seul moyen de s'échapper était de vaincre les cent niveaux 
        du donjon et battre le boss final."""
    }
]

def test_analyze(name, text):
    print(f"\n{'='*60}")
    print(f"Testing: {name}")
    print(f"Text length: {len(text)} characters")
    print(f"{'='*60}")
    
    try:
        response = requests.post(
            f"{BASE_URL}/analyze",
            json={"text": text},
            headers={"Content-Type": "application/json"}
        )
        
        if response.status_code == 200:
            data = response.json()
            
            print(f"✓ Success!")
            print(f"  Processing time: {data['processing_time_ms']}ms")
            print(f"  Difficulty score: {data['difficulty_score']}")
            print(f"  Total words: {data['statistics']['total_words']}")
            print(f"  Unique lemmas: {data['statistics']['unique_lemmas']}")
            print(f"  Sentences: {data['statistics']['sentences']}")
            print(f"  Avg sentence length: {data['statistics']['avg_sentence_length']}")
            print(f"  Complex structures: {data['statistics']['dependency_complexity']['complex_structures']}")
            print(f"  Top 5 hardest words:")
            for i, word in enumerate(data['top_words'][:5], 1):
                print(f"    {i}. {word['word']} (rank: {word['frequency_rank']}, {word['pos']})")
        else:
            print(f"✗ Error: {response.status_code}")
            print(response.text)
    
    except Exception as e:
        print(f"✗ Exception: {e}")

def test_health():
    print("\n" + "="*60)
    print("Testing health endpoint")
    print("="*60)
    try:
        response = requests.get(f"{BASE_URL}/health")
        print(f"Health check: {response.json()}")
    except Exception as e:
        print(f"✗ Cannot connect to server: {e}")
        print("Make sure the server is running!")
        return False
    return True

if __name__ == "__main__":
    print("\n🚀 FRENCH ANALYZER NLP WORKER TEST SUITE")
    
    if not test_health():
        exit(1)
    
    for test in test_cases:
        test_analyze(test["name"], test["text"])
    
    print("\n" + "="*60)
    print("✓ All tests completed!")
    print("="*60 + "\n")