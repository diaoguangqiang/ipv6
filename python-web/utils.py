'''
# 山东青年政治学院
# 
'''
import time
import pymysql
import cv2
import numpy as np
#结构体
import struct
import socket

def get_time():
    time_str = time.strftime("%Y{}%m{}%d{} %X")
    return time_str.format("年","月","日")

'''
# 建立与数据库的连接
# 数据库地址：127.0.0.1
# 数据库名字：cov
# 数据库信息：root/123456
# 返回连接和游标
'''
def get_conn():
    # 建立连接
    conn = pymysql.connect(host="127.0.0.1", user="root", password="123456", db="cov", charset="utf8")
    # 创建游标A
    cursor = conn.cursor()
    return conn, cursor

'''
# 关闭数据库的连接
'''
def close_conn(conn, cursor):
    if cursor:
        cursor.close()
    if conn:
        conn.close()

def query(sql,*args):
    """

    :param sql:
    :param args:
    :return:
    """
    conn,cursor = get_conn()
    cursor.execute(sql,args)
    res = cursor.fetchall()
    close_conn(conn,cursor)
    return res

def test():
    sql = "select * from details"
    res = query(sql)
    return res[0]

def insert_totableTH():
    db = pymysql.connect(host='127.0.0.1', port=3306, user='root', password='123456', database='cov')
    sql = 'INSERT INTO warning (warning_type,time,warninng_flag,deal_flag,month) VALUES(2,NOW(),1,0,5);'
    print(sql)
    cursor = db.cursor()
    try:
        cursor.execute(sql)
        db.commit()
    except:
        print('failed!')
        db.rollback()

    db.close()
    print('ok')

    return

def get_c1_data():
    sql = "select (select confirm from history order by ds desc limit 1)," \
          "(select suspect from history order by ds desc limit 1)," \
          "sum(heal),sum(dead) from details " \
          "where update_time=(select update_time from details order by update_time desc limit 1) "
    print(sql)
    res = query(sql)
    #print(res)
    if res[0][0]>30 or res[0][1]>70 :
        print('insert to warning')
        insert_totableTH()
    return res[0]

def get_c2_data():
    sql = "select province,sum(confirm) from details " \
          "where update_time=(select update_time from details " \
          "order by update_time desc limit 1) " \
          "group by province"
    print(sql)
    res = query(sql)
    return res

def get_l1_data():
    sql = "select ds,confirm_add,suspect_add,heal,dead from history"
    res = query(sql)
    return res[-20:]

def get_l2_data():
    sql = "select ds,confirm,suspect from history"
    res = query(sql)
    return res[-20:]

def get_r1_data():
    sql='select city,confirm from details ORDER BY confirm DESC;'
    sql='SELECT month ,count(*) AS COUNT FROM warning GROUP BY month ORDER BY count DESC;'
    print(sql)
    res = query(sql)

    return res

def get_r2_data():
    sql = "select content from hotsearch order by id desc limit 20"
    res = query(sql)

    return res


def get_newface():
    sql = "select filepath from face_info order by time DESC LIMIT 1"
    res = query(sql)
    return res

def imgReload():
    filepath=get_newface()
    src='<img src="{}" style="width: 80%; height: 100%; position: absolute;">'.format(filepath)

    return src
def insertimg(location,filename):
    try:
        conn = pymysql.connect(host="127.0.0.1", port=3306, user="root", password="123456", db="cov")
        print('success')
    except:
        print('failed')
    cursor = conn.cursor()
    sql = """ INSERT INTO face(time,location,filename)VALUES(NOW(), """ + "'{}'".format(
        location) + "," + "'{}.png'".format(filename) + ");"

    try:
        cursor.execute(sql)
        conn.commit()
    except:
        conn.rollback()
    conn.close()

def getimginfo():
        sql="""SELECT filename,time FROM face ORDER BY time DESC LIMIT 1;"""
        res = query(sql)
        return res
"""
img_encode img_decode为图像网络传输编解码函数
"""
def img_encode(img):
    """The method encodes img to jepg, amd then make it to string"""
    imgencode = cv2.imencode('.jpg', img)[1]
    data_encode = np.array(imgencode)
    str_encode = data_encode.tobytes()
    return str_encode

def img_decode(str_encode):
    """The method decode string to image"""
    image = np.asarray(bytearray(str_encode), dtype='uint8')
    print(image)
    image = cv2.imdecode(image, cv2.IMREAD_COLOR)
    return image



def img_encode(img):
    """The method encodes img to jepg, amd then make it to string"""
    imgencode = cv2.imencode('.jpg', img)[1]
    data_encode = np.array(imgencode)
    str_encode = data_encode.tobytes()
    return str_encode

def img_decode(str_encode):
    """The method decode string to image"""
    image = np.asarray(bytearray(str_encode), dtype='uint8')
    print(image)
    image = cv2.imdecode(image, cv2.IMREAD_COLOR)
    return image

def recvimg(sock):
    s, addr = sock.accept()
    #print(addr)
    size = s.recv(struct.calcsize('l'))
    print('size:', size)
    sizedata = struct.unpack('l', size)[0]
    print('sizedata:', sizedata)
    #####################################
    blocksize = 1024
    blocknum = sizedata // blocksize
    block_rest = sizedata % blocksize

    #print("blocknum,blockrest:", blocknum, block_rest)

    databuf = b''
    if blocknum != 0:
        for i in range(blocknum):
            databuf += s.recv(blocksize)
            # print(databuf)
    databuf += s.recv(block_rest)
    s.close()
    data = databuf
    databuf = b''
    #print("data:", data)
    #s.send(b'ok')
    s.close()
    img = img_decode(data)
    #imshow(img)
    return img

def streamRecv(IP = '0.0.0.0',PORT = 8000):
    # IP = '192.168.31.51'
    # PORT = 8000
    server = (IP, PORT)
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    try:

        sock.bind(server)
        sock.listen(3)
        print('start...')
    except:
        print('con failed')

    while True:
        print('you can send img')
        img = recvimg(sock)
        cv2.imshow('img', img)
        if cv2.waitKey(1) & 0xFF == ord('q'):
            break
    cv2.destroyAllWindows()
    sock.close
    input('over')

    return

"""
模块关联说明：
img_segment,face为人脸识别与保存，保存路径为前者的targetdir,文件名为日期
"""
def img_segment(img):
#    img = cv2.imread()
    targetdir='C:/Users/NiuChunchao/Downloads/Cov/Cov/static/img/'
    print(img.shape)
    cropped = img
    filename=str(time.strftime('%m-%d_%H%M%S'))
    try:
        cv2.imwrite(targetdir+'{}.png'.format(filename),cropped)
        insertimg(location=targetdir,filename=filename)
        print('img written!')
        return cropped
    except:
        print('failed in writing picture to location.')

def face(img):
    color=(0,255,0)
    grey = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    classfier = cv2.CascadeClassifier("haarcascade_frontalface_alt.xml")

    faceRects = classfier.detectMultiScale(grey, scaleFactor=1.2, minNeighbors=3, minSize=(32, 32))
    if len(faceRects) > 0:  # 大于0则检测到人脸
        for faceRect in faceRects:  # 单独框出每一张人脸
            x, y, w, h = faceRect
            cv2.rectangle(img, (x - 10, y - 10), (x + w + 10, y + h + 10), color, 3)  # 5控制绿色框的粗细

        return True,img
    return False,img
"""
获取图像流并将图像通过video_feed进行分发
"""
def gen_frames():

    IP = '0.0.0.0'
    PORT = 8000
    server = (IP, PORT)
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    try:
        sock.bind(server)
        sock.listen(3)
        print('start...')
    except:
        print('con failed')
    #################################人脸存在短路标记############################
    flagA = False
    flagB = False
    #######################################################################
    while True:
        success=True
        frame = recvimg(sock)  # read the camera frame
        bo, frame = face(frame)
        flagB = bo

        if flagB == True and flagA == False:
            print('face')
            img_segment(frame)
        flagA = flagB
        if not success:
            break
        else:
            ret, buffer = cv2.imencode('.jpg', frame)
            frame = buffer.tobytes()
            yield (b'--frame\r\n'
                   b'Content-Type: image/jpeg\r\n\r\n' + frame + b'\r\n')  # concat frame one by one and show result

def countwarning():
    sql = "SELECT COUNT(*) FROM warning;"
    res = query(sql)
    return res[0][0]

def smokeupdate():
    db = pymysql.connect(host='127.0.0.1', port=3306, user='root', password='123456', database='cov')
    sql = 'UPDATE warning set deal_flag = 1 WHERE warning_type =3 ORDER BY time DESC LIMIT 1;'
    print(sql)
    cursor = db.cursor()
    try:
        cursor.execute(sql)
        db.commit()
    except:
        print('failed!')
        db.rollback()

    db.close()
    print('ok')

    return

def getsmoke():
    sql = "SELECT id,warning_type,warninng_flag,deal_flag FROM warning where warning_type = 3 ORDER BY time DESC;"
    res = query(sql)
    print(res[0])
    if res[0][2] != res[0][3]:
        smokeupdate()
        return res[0][2]
    else:
        return 0

if __name__ == "__main__":
    #print(get_l2_data()[-10:])
    print(get_c1_data())
    #print(str(getsmoke()))
   # insertimg('../','')
    #print(getimginfo()[0][0])
    #print( imgReload())
    #print(test())