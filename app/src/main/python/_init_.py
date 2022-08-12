from chatterbot import ChatBot
import nltk

def nullfunc(str):
    return
def start(db):
    nltk.download = nullfunc
    global chatbot
    chatbot = ChatBot(
        '$$name$$',
        storage_adapter='chatterbot.storage.SQLStorageAdapter',
        logic_adapters=[
            'chatterbot.logic.MathematicalEvaluation',
            'chatterbot.logic.BestMatch'
        ],
        database_uri='sqlite:///'+db
    )


