from chatterbot import ChatBot
import nltk
import os

def nullfunc(str):
    return
nltk.download = nullfunc

clear = lambda: os.system('cls')
clear()
chatbot = ChatBot(
    '$$name$$',
    storage_adapter='chatterbot.storage.SQLStorageAdapter',
    logic_adapters=[
        'chatterbot.logic.MathematicalEvaluation',
        'chatterbot.logic.BestMatch'
    ],
    database_uri='sqlite:///dblocation'
)

bot_response = chatbot.get_response("message")

print(str(bot_response))