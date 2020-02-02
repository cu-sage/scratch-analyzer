# add file folder to system path
import sys, os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath("server_interface"))))

# encoding=utf-8
import os
import sys

from behaviordetection.Models import predict
from flask import Flask, request, jsonify, make_response
from flask_cors import *

from sagenode_interface import SageDb, ObjectId
from server_interface.poisson_path import get_hints, build_graph, Graph

application = Flask(__name__)
CORS(application)


@application.route('/', methods=['GET'])
def build():
    """
    NOT FUNCTIONAL

    In the future, this request will use mock data (or real data) to update the
    graphs used in the model. It cannot be done in realtime for each hint
    request, so it is up to some external trigger to determine how often the
    graphs will be updated.
    """
    build_graph()
    return make_response(jsonify('built graph!'), 200)


@application.route('/get_data', methods=['GET'])
def read():
    """
    LEGACY

    Gets `gameid` from query, returns corresponding game object from database
    """
    id = request.args.get('gameid')
    db = SageDb()
    result = db.read(ObjectId(id))
    return make_response(jsonify({'result': str(result)}), 200)


@application.route('/save', methods=['POST'])
def save():
    """
    LEGACY
    
    Saves hint object to database, returns corresponding game object to database
    """
    try:
        content = request.json
        hints = content['hints']

        db = SageDb()
        hint = str(hints)
        game_id = db.save(hint)
        content = db.read(game_id)

        return make_response(jsonify({'result': str(content)}), 200)

    except Exception as e:
        print(e)
        return make_response(jsonify([]), 200)


@application.route('/get_hints', methods=['POST'])
def hint():
    """
    Generates hints for a certain puzzle, current snapshot, and student type
    based on models built on mock / real game data
    """
    try:
        content = request.json # request in readable format
        seFiles = content['seFiles']
        se_string = seFiles[-1]['content'] # last update is the current snapshot
        sb_type = content['info']['studentType'] # student behavior type
        puzzle_id = content['puzzleID'] # name / id of the puzzle we are talking about eg. 'face_morphing'
        hints = get_hints(puzzle_id, se_string, sb_type)
        return make_response(jsonify({"hints": hints}), 200)
    except Exception as e:
        print(e)
        return make_response(jsonify([]), 200)


@application.route('/get_behavior_type', methods=['POST'])
def get_behavior_classified():
    """
    NOT FUNCTIONAL

    Given student se file, return the type of behavior

    Note: Inherited from Spring 2018 (Fall 2018)
    """
    content = request.json
    '''***this need to be replace with the json/other object pass from sage_node***'''
    temp_path_to_test = content['test_file']
    y = predict(temp_path_to_test)
    '''***the end of the things need to be changed***'''
    return make_response(jsonify(y), 200)


if __name__ == "__main__":
    # Setting debug to True enables debug output. This line should be
    # removed before deploying a production app.

    application.debug = True
    application.run(host='localhost', port=5000)
