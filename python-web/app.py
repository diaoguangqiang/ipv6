from flask import Flask as _Flask,jsonify
from flask import request
from flask import render_template
from flask.json import JSONEncoder as _JSONEncoder
from jieba.analyse import extract_tags
from threading import Thread
import decimal
import utils
import string
import flask

class JSONEncoder(_JSONEncoder):
        def default(self, o):
            if isinstance(o, decimal.Decimal):
                return float(o)
            super(_JSONEncoder, self).default(o)

class Flask(_Flask):
    json_encoder = JSONEncoder


app = Flask(__name__)


@app.route('/hello')
def hello_world():
    return 'Hello World!'

@app.route('/login')
def hello_world2():
    name = request.values.get("name")
    pwd = request.values.get("pwd")
    return f'name={name}.pwd={pwd}'

@app.route('/abc')
def hello_world1():
    id = request.values.get("id")
    return f"""
    <form action="/login">
            账号：<input name="name"><br>
            密码：<input name="pwd"> 
            <input type="submit">
        </form>
    """

@app.route('/')
def hello_word3():
    return render_template("main.html")
@app.route('/test')
def test():
    return render_template("test.html")

@app.route('/ajax',methods=["get","post"])
def hello_word4():
    return '10000'

@app.route('/time')
def get_time():
    return utils.get_time()

@app.route('/c1')
def get_c1_data():
    data = utils.get_c1_data()
    return jsonify({"confirm":data[0],"suspect":data[1],"heal":data[2],"dead":data[3]})

@app.route('/c2')
def get_c2_data():
    res = []
    for tup in utils.get_c2_data():
        res.append({"name":tup[0],"value":int(tup[1])})
    return jsonify({"data":res})

@app.route('/l1')
def get_l1_data():
    data = utils.get_l1_data()
    print(data)
    day,confirm,suspect,heal,dead = [],[],[],[],[]
    for a,b,c,d,e in data[7:]:
        day.append(a.strftime("%m-%d"))
        confirm.append(b)
        suspect.append(c)
        # heal.append(d)
        # dead.append(e)
    # return jsonify({"day":day,"confirm":confirm,"suspect":suspect,"heal":heal,"dead":dead})
    #
    return jsonify({"day": day, "confirm": confirm,"suspect":suspect})

@app.route('/l2')
def get_l2_data():
    data = utils.get_l2_data()
    day,confirm_add,suspect_add = [],[],[]
    for a,b,c in data[7:]:
        day.append(a.strftime("%m-%d"))
        confirm_add.append(b)
        suspect_add.append(c)
    return jsonify({"day":day,"confirm_add":confirm_add,"suspect_add":suspect_add})

@app.route('/r1')
def get_r1_data():
    data = utils.get_r1_data()
    city = []
    confirm = []
    for k,v in data:
        city.append(k)
        confirm.append(int(v))
    return jsonify({"city": city,"confirm": confirm})

@app.route('/r2')
def get_r2_data():
    data = utils.get_r2_data()
    d = []
    for i in data:
        k = i[0].rstrip(string.digits)
        v = i[0][len(k):]
        ks = extract_tags(k)
        for j in ks:
            if not j.isdigit():
                d.append({"name": j,"value": v})
    return jsonify({"kws": d})

@app.route('/getsmoke')
def getsmoke():
    smoke = utils.getsmoke()
    return '<h1>{}</h1>'.format(smoke)

@app.route('/getcountwarning')
def getcount():
    count = utils.countwarning()
    return '<h1>{}</h1>'.format(count)

@app.route('/getimg')
def reimg():
    print('---------------------------------------------------------------------------')
    src='../static/img/'+str(utils.getimginfo()[0][0])
    img="""<img id="img" src="{}" 
			style="width:80%; height:100%; position: absolute;left: 20%;">""".format(src)
    print(img)
    return img

@app.route('/video_feed')
def video_feed():
    return flask.Response(utils.gen_frames(), mimetype='multipart/x-mixed-replace; boundary=frame')

@app.route('/getIP')
def getIP():
    ip = request.remote_addr
    print(type(ip))
    print('ip:',ip)
    if ':' in ip:
        ip = '您正在以IPv6协议访问，您的IPv6 address:' + ip
        return ip
    else:
        ip = '您正在以IPv4协议访问，您的IPv4 address:' + ip
        return ip
    return ip



if __name__ == '__main__':
    th1 = Thread(target=app.run, args=("0.0.0.0", 80))
    th2 = Thread(target=app.run,args = ("::", 80))
    # app.run(host="::", port=5000)
    # app.run(host="0.0.0.0", port=80)
    th2.start()
    th1.start()
