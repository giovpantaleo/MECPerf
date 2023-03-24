import json 


HEADER = '\033[95m'
OKBLUE = '\033[94m'
OKGREEN = '\033[92m'
WARNING = '\033[93m'
FAIL = '\033[91m'
ENDC = '\033[0m'
BOLD = '\033[1m'
UNDERLINE = '\033[4m'


INTERNAL_SERVER_ERROR = 500
NOT_IMPLEMENTED = 501
OK = 200

def build_active_bandwidth_query(json, keyword, likeKeyword, fromInterval, toInterval, command, direction, group_by):
    params = []
    query = "SELECT "
    
    if json == "True" or json == "true":
        query += " JSON_OBJECT ( "
        query += " 'TestNumber', TestNumber, "
        query += " 'ID', Test.ID,"
        query += " 'Timestamp', Timestamp, "
        query += " 'Direction', Direction, "
        query += " 'Command', Command, "
        query += " 'SenderIdentity', SenderIdentity,  "
        query += " 'ReceiverIdentity', ReceiverIdentity, "
        query += " 'SenderIPv4Address', SenderIPv4Address, "
        query += " 'ReceiverIPv4Address', ReceiverIPv4Address,"
        query += " 'Keyword', Keyword, "
        query += " 'PackSize', PackSize, " 
        query += " 'NumPack', NumPack, "
        if group_by ==True:
            query += " 'Bandwidth [bit/s]',  (1.0 * (SUM(kBytes * 1024 * 8))/(1.0 * SUM(1.0 * nanoTimes / 1000000000))) "
        else:
            query += " 'Bandwidth [bit/s]',  (1.0 * (kBytes * 1024 * 8)/(1.0 * 1.0 * nanoTimes / 1000000000)) "
        query += " ) "
    else:
        json = str(False)
        query += "TestNumber, Test.ID, Timestamp, Direction, Command, SenderIdentity, ReceiverIdentity, "
        query += "SenderIPv4Address, ReceiverIPv4Address, Keyword, PackSize, NumPack "

    whereClause = False
    query += "FROM Test INNER JOIN BandwidthMeasure ON Test.ID = BandwidthMeasure.id "

    
    if (keyword != None):
        query += " where Keyword = %s"
        params.append(keyword)
        whereClause = True

    if (likeKeyword != None):
        if (whereClause == False):
            query += " where "
            whereClause = True
        else:
            query += " AND "

        query += " Keyword LIKE %s"

        likeKeyword = "%" + likeKeyword + "%"
        params.append(likeKeyword)

    if (fromInterval != None):
        if (whereClause == False):
            query += " where "
            whereClause = True
        else:
            query += " AND "

        query += " Timestamp > %s"
        params.append(fromInterval)

    if (toInterval != None):
        if (whereClause == False):
            query += " where "
            whereClause = True
        else:
            query += " AND "

        query += "Timestamp < %s"
        params.append(toInterval)
    
    if (command != None):
        if (whereClause == False):
            query += " where "
            whereClause = True
        else:
            query += " AND "

        query += "Command = %s"
        params.append(command)
    
    if (direction != None):
        if (whereClause == False):
            query += " where "
            whereClause = True
        else:
            query += " AND "

        query += " Direction = %s"
        params.append(direction)

    if group_by == True:
        query += " GROUP BY Test.ID"



    return json, query, params



def build_passive_bandwidth_query(mode, json, keyword, likeKeyword, fromInterval, toInterval, direction, 
                                  dashfilename, numberofclients, limit, offset):
    params = []
    query = "SELECT "
    
    if json == "True" or json == "true":
        query += " JSON_OBJECT ( "
        query += " 'ID', PassiveTest.ID," 
        query += " 'Date', PassiveTest.Timestamp,"       
        query += " 'ClientIP', ClientIP, "
        query += " 'ClientPort', ClientPort, "
        query += " 'ServerIP', ServerIP, "
        query += " 'ServerPort', ServerPort, "
        query += " 'Keyword', Keyword, "
        query += " 'Direction', Direction, "
        query += " 'Protocol', Protocol, "
        query += " 'Mode', Mode,  "
        query += " 'Type', Type, "
        query += " 'Timestamp', PassiveBandwidthMeasure.Timestamp,"
        if mode == "self":
            query += " 'Kbps', Bytes "
        elif mode == "mim":
            query += " 'Bytes', Bytes "
        query += " ) "
    else:
        json = str(False)
        query += "PassiveTest.ID, ClientIP, ClientPort, ServerIP, ServerPort, Keyword, Direction, Protocol, "
        query += " Mode, Type, PassiveBandwidthMeasure.Timestamp, Bytes "

    
    query += "FROM PassiveTest INNER JOIN PassiveBandwidthMeasure "
    query += "ON PassiveTest.ID = PassiveBandwidthMeasure.id "
    query += "WHERE Mode = %s "
    params.append(mode)
    
    if (keyword != None):
        query += "AND Keyword = %s "
        params.append(keyword)

    if (likeKeyword != None):
        query += "AND Keyword LIKE %s "
        params.append("%" + likeKeyword + "%")

    if (dashfilename != None):
        print (dashfilename)
        query += "AND Keyword LIKE %s "
        params.append("%" + dashfilename + "%")

    if (numberofclients != None):
        print (numberofclients)
        query += "AND Keyword LIKE %s "
        params.append("%" + numberofclients + "client%")

    if (fromInterval != None):
        query += "AND PassiveTest.Timestamp > %s "
        params.append(fromInterval)

    if (toInterval != None):
        query += "AND PassiveTest.Timestamp < %s "
        params.append(toInterval)
    
    if (direction != None):
        query += "AND Direction = %s "
        params.append(direction)
    
    if limit != None:
        print (type(limit))
        query += "LIMIT %s "
        params.append(int(limit))
        
        if offset != None:
            print (type(offset))
            query += "OFFSET %s "
            params.append(int(offset))


    print (query)
    return json, query, params



def read_last_test_number(mysql):
    query = "SELECT TestNumber FROM MECPerf.Test ORDER BY ID desc limit 1"
    cur = mysql.connection.cursor()
    cur.execute(query, [])
    query_res = cur.fetchone()
    if (query_res is None or len(query_res) == 0 ) :
        cur.close()
        return 1

    cur.close()
    return query_res[0] + 1
    
    

def insert_test_table(cur, actual_test_number, test_type, test_info, direction = "", measure_type = ""):
    insert_Test_table_query_active = "INSERT INTO MECPerf.Test (TestNumber,Timestamp, Direction, Command, " +\
                                                        " SenderIdentity, ReceiverIdentity, " +\
                                                        " SenderIPv4Address, ReceiverIPv4Address, Keyword, " +\
                                                        " PackSize, NumPack) VALUES (%s, CURRENT_TIMESTAMP," +\
                                                        " %s, %s, %s, %s, %s, %s, %s, %s, %s)"
    insert_Test_table_query_passive = "INSERT INTO MECPerf.PassiveTest (Timestamp, ClientIP, ClientPort, "+\
                                                        "ServerIP, ServerPort, Keyword, Direction, "+\
                                                        "Protocol, Mode, Type) VALUES (CURRENT_TIMESTAMP, "+\
                                                        " %s, %s, %s, %s, %s, %s, %s, %s, %s)"

    if test_type == "active":
        cur.execute(insert_Test_table_query_active, [actual_test_number, test_info['Direction'], 
                    test_info['Command'], test_info['SenderIdentity'], test_info['ReceiverIdentity'], 
                    test_info['SenderIPv4Address'], test_info['ReceiverIPv4Address'], test_info['Keyword'], 
                    test_info['PackSize'], test_info['NumPack']])
    if test_type == "passive":
        cur.execute(insert_Test_table_query_passive, [ test_info['client_ip'], test_info['client_port'], 
                    test_info['server_ip'], test_info['server_port'], test_info['service'], 
                    direction, test_info['protocol'], test_info['mode'], measure_type])




def insert_bandwidth_table(cur, rowID, test_type, test_values):
    insert_BandwidthMeasure_table_active = "INSERT INTO MECPerf.BandwidthMeasure VALUES (%s, %s, %s, %s)"
    insert_BandwidthMeasure_table_passive = "INSERT INTO MECPerf.PassiveBandwidthMeasure (ID, Timestamp, bytes) VALUES (%s, %s, %s)"

    if test_type == "active":
        for i in range(0, len(test_values)):
            args = [rowID, i + 1,  int(test_values[i]['nanoTimes']), round(float(test_values[i]['kBytes']), 4)]
                    
            cur.execute(insert_BandwidthMeasure_table_active, args)

    if test_type == "passive":
        for timestamp in test_values:
            args = [rowID, int(timestamp), int(test_values[timestamp])]           
            cur.execute(insert_BandwidthMeasure_table_passive, args)


def insert_latency_table(cur, rowID, test_type, test_values):
    insert_latency_query_active = "INSERT INTO MECPerf.RttMeasure (id, sub_id, latency, timestamp_millis) VALUES (%s, %s, %s, %s)"
    insert_latency_query_passive = "INSERT INTO MECPerf.PassiveRttMeasure (ID, Timestamp, latency) VALUES (%s, %s, %s)"

    if test_type == "active":
        for i in range(0, len(test_values)):
            args = [rowID, int(test_values[i]['sub_id']), float(test_values[i]['latency']), 
                    int(test_values[i]['timestamp_millis'])]
            cur.execute(insert_latency_query_active, args)

    if test_type == "passive":
        for timestamp in test_values:
            args = [rowID, long(timestamp),  float(test_values[timestamp])]
            cur.execute(insert_latency_query_passive, args)


def insert_metadata_table(cur, rowID, test_type, test_metadata, key):
    assert test_type == "active"

    insert_metadata_query_active = "INSERT INTO ExperimentMETADATA (id, experiment_details) VALUES (%s, %s)"
    #print(json.dumps(test_metadata))
    args = [rowID, json.dumps(test_metadata)]

    cur.execute(insert_metadata_query_active, args)
    

def update_bandwidth(test, mysql):
    if test.type == "active":
        return update_active_bandwidth(test, mysql)
    elif test.type == "passive":
        return update_passive_bandwidth(test, mysql)
    else:
        print ("\n\n" + FAIL + "ERROR unknown test.type" + ENDC)
        return 501

                



def update_latencies(test, mysql):
    if test.type == "active":
        return update_active_latencies(test, mysql)
    elif test.type == "passive":
        return update_passive_latencies(test, mysql) 
    else:
        print ("\n\n" + FAIL + "ERROR unknown test.type" + ENDC  )  
        return NOT_IMPLEMENTED


def update_active_bandwidth(test, mysql):
    actual_test_number = read_last_test_number(mysql)
    
    mysql.connection.autocommit = False
    try:
        cur = mysql.connection.cursor()

        #store first segment measures
        insert_test_table(cur, actual_test_number, test.type, test.test_info_first_segment)
        last_rowID = int(cur.lastrowid)
        insert_bandwidth_table(cur, last_rowID, test.type, test.test_values_first_segment)
        if (test.metadata_first_segment != None):
            insert_metadata_table(cur, last_rowID, test.type, test.metadata_first_segment, "metadata_first_segment")

        #update second segment measures
        insert_test_table(cur, actual_test_number, test.type, test.test_info_second_segment)
        last_rowID = int(cur.lastrowid)
        insert_bandwidth_table(cur, last_rowID, test.type, test.test_values_second_segment)
        if (test.metadata_second_segment != None):
            insert_metadata_table(cur, last_rowID, test.type, test.metadata_first_segment, "metadata_second_segment")


        mysql.connection.commit()
        mysql.connection.autocommit=True
        cur.close()
    
    except mysql.connection.Error as e:
        print (test.test_info_first_segment)
        print (test.test_info_second_segment)
        print (FAIL + "Failed to update the database.")
        print (e)
        print ("Execute a roll back" + ENDC)
        mysql.connection.rollback()
        cur.close()

        return INTERNAL_SERVER_ERROR
    
    return OK



def update_active_latencies(test, mysql):
    actual_test_number = read_last_test_number(mysql)
    
    mysql.connection.autocommit = False
    try:
        cur = mysql.connection.cursor()

        #store first segment measures
        insert_test_table(cur, actual_test_number, test.type, test.test_info_first_segment)
        last_rowID = int(cur.lastrowid)
        insert_latency_table(cur, last_rowID , test.type, test.test_values_first_segment)
        if (test.metadata_first_segment != None):
            insert_metadata_table(cur, last_rowID, test.type, test.metadata_first_segment, "metadata_first_segment")

        #update second segment measures
        insert_test_table(cur, actual_test_number, test.type, test.test_info_second_segment)
        last_rowID = int(cur.lastrowid)
        insert_latency_table(cur, last_rowID, test.type, test.test_values_second_segment)
        if (test.metadata_second_segment != None):
            insert_metadata_table(cur, last_rowID, test.type, test.metadata_second_segment, "metadata_second_segment")


        mysql.connection.commit()
        mysql.connection.autocommit=True
        cur.close()
    
    except mysql.connection.Error as e:
        print ("Failed to update the database.")
        print (e)
        print ("Execute a roll back")
        mysql.connection.rollback()
        cur.close()
        return INTERNAL_SERVER_ERROR

    return OK



def update_passive_bandwidth(test, mysql):
    mysql.connection.autocommit = False
    try:
        cur = mysql.connection.cursor()

        if len(test.uplink) != 0:
            insert_test_table(cur, -1, test.type, test.info, 'uplink', 'bandwidth')
            insert_bandwidth_table(cur, cur.lastrowid, test.type, test.uplink)
        if len(test.downlink) != 0:
            insert_test_table(cur, -1, test.type, test.info, 'downlink', 'bandwidth')
            insert_bandwidth_table(cur, cur.lastrowid, test.type, test.downlink)

        mysql.connection.commit()
        mysql.connection.autocommit=True
        cur.close()
    
    except mysql.connection.Error as e:
        print (FAIL + "Failed to update the database.")
        print (e)
        print ("Execute a roll back" + ENDC)
        mysql.connection.rollback()
        cur.close()

        return INTERNAL_SERVER_ERROR
    
    return OK


def update_passive_latencies(test, mysql):
    mysql.connection.autocommit = False
    try:
        cur = mysql.connection.cursor()

        if len(test.uplink) != 0:
            insert_test_table(cur, -1, test.type, test.info, 'uplink', 'latency')
            insert_latency_table(cur, cur.lastrowid, test.type, test.uplink)
        if len(test.downlink) != 0:
            insert_test_table(cur, -1, test.type, test.info, 'downlink', 'latency')
            insert_latency_table(cur, cur.lastrowid, test.type, test.downlink)


        mysql.connection.commit()
        mysql.connection.autocommit=True
        cur.close()
    
    except mysql.connection.Error as e:
        print ("Failed to update the database.")
        print (e)
        print ("Execute a roll back")
        mysql.connection.rollback()
        cur.close()
        return INTERNAL_SERVER_ERROR

    return OK






'''
OLD
import json 


HEADER = '\033[95m'
OKBLUE = '\033[94m'
OKGREEN = '\033[92m'
WARNING = '\033[93m'
FAIL = '\033[91m'
ENDC = '\033[0m'
BOLD = '\033[1m'
UNDERLINE = '\033[4m'


INTERNAL_SERVER_ERROR = 500
NOT_IMPLEMENTED = 501
OK = 200

def build_active_bandwidth_query(json, keyword, likeKeyword, fromInterval, toInterval, command, direction, group_by):
    params = []
    query = "SELECT "
    
    if json == "True" or json == "true":
        query += " JSON_OBJECT ( "
        query += " 'TestNumber', TestNumber, "
        query += " 'ID', Test.ID,"
        query += " 'Timestamp', Timestamp, "
        query += " 'Direction', Direction, "
        query += " 'Command', Command, "
        query += " 'SenderIdentity', SenderIdentity,  "
        query += " 'ReceiverIdentity', ReceiverIdentity, "
        query += " 'SenderIPv4Address', SenderIPv4Address, "
        query += " 'ReceiverIPv4Address', ReceiverIPv4Address,"
        query += " 'Keyword', Keyword, "
        query += " 'PackSize', PackSize, " 
        query += " 'NumPack', NumPack, "
        if group_by ==True:
            query += " 'Bandwidth [bit/s]',  (1.0 * (SUM(kBytes * 1024 * 8))/(1.0 * SUM(1.0 * nanoTimes / 1000000000))) "
        else:
            query += " 'Bandwidth [bit/s]',  (1.0 * (kBytes * 1024 * 8)/(1.0 * 1.0 * nanoTimes / 1000000000)) "
        query += " ) "
    else:
        json = str(False)
        query += "TestNumber, Test.ID, Timestamp, Direction, Command, SenderIdentity, ReceiverIdentity, "
        query += "SenderIPv4Address, ReceiverIPv4Address, Keyword, PackSize, NumPack "

    whereClause = False
    query += "FROM Test INNER JOIN BandwidthMeasure ON Test.ID = BandwidthMeasure.id "

    
    if (keyword != None):
        query += " where Keyword = %s"
        params.append(keyword)
        whereClause = True

    if (likeKeyword != None):
        if (whereClause == False):
            query += " where "
            whereClause = True
        else:
            query += " AND "

        query += " Keyword LIKE %s"

        likeKeyword = "%" + likeKeyword + "%"
        params.append(likeKeyword)

    if (fromInterval != None):
        if (whereClause == False):
            query += " where "
            whereClause = True
        else:
            query += " AND "

        query += " Timestamp > %s"
        params.append(fromInterval)

    if (toInterval != None):
        if (whereClause == False):
            query += " where "
            whereClause = True
        else:
            query += " AND "

        query += "Timestamp < %s"
        params.append(toInterval)
    
    if (command != None):
        if (whereClause == False):
            query += " where "
            whereClause = True
        else:
            query += " AND "

        query += "Command = %s"
        params.append(command)
    
    if (direction != None):
        if (whereClause == False):
            query += " where "
            whereClause = True
        else:
            query += " AND "

        query += " Direction = %s"
        params.append(direction)

    if group_by == True:
        query += " GROUP BY Test.ID"



    return json, query, params



def build_passive_bandwidth_query(mode, json, keyword, likeKeyword, fromInterval, toInterval, direction, 
                                  dashfilename, numberofclients, limit, offset):
    params = []
    query = "SELECT "
    
    if json == "True" or json == "true":
        query += " JSON_OBJECT ( "
        query += " 'ID', PassiveTest.ID," 
        query += " 'Date', PassiveTest.Timestamp,"       
        query += " 'ClientIP', ClientIP, "
        query += " 'ClientPort', ClientPort, "
        query += " 'ServerIP', ServerIP, "
        query += " 'ServerPort', ServerPort, "
        query += " 'Keyword', Keyword, "
        query += " 'Direction', Direction, "
        query += " 'Protocol', Protocol, "
        query += " 'Mode', Mode,  "
        query += " 'Type', Type, "
        query += " 'Timestamp', PassiveBandwidthMeasure.Timestamp,"
        if mode == "self":
            query += " 'Kbps', Bytes "
        elif mode == "mim":
            query += " 'Bytes', Bytes "
        query += " ) "
    else:
        json = str(False)
        query += "PassiveTest.ID, ClientIP, ClientPort, ServerIP, ServerPort, Keyword, Direction, Protocol, "
        query += " Mode, Type, PassiveBandwidthMeasure.Timestamp, Bytes "

    
    query += "FROM PassiveTest INNER JOIN PassiveBandwidthMeasure "
    query += "ON PassiveTest.ID = PassiveBandwidthMeasure.id "
    query += "WHERE Mode = %s "
    params.append(mode)
    
    if (keyword != None):
        query += "AND Keyword = %s "
        params.append(keyword)

    if (likeKeyword != None):
        query += "AND Keyword LIKE %s "
        params.append("%" + likeKeyword + "%")

    if (dashfilename != None):
        print (dashfilename)
        query += "AND Keyword LIKE %s "
        params.append("%" + dashfilename + "%")

    if (numberofclients != None):
        print (numberofclients)
        query += "AND Keyword LIKE %s "
        params.append("%" + numberofclients + "client%")

    if (fromInterval != None):
        query += "AND PassiveTest.Timestamp > %s "
        params.append(fromInterval)

    if (toInterval != None):
        query += "AND PassiveTest.Timestamp < %s "
        params.append(toInterval)
    
    if (direction != None):
        query += "AND Direction = %s "
        params.append(direction)
    
    if limit != None:
        print (type(limit))
        query += "LIMIT %s "
        params.append(int(limit))
        
        if offset != None:
            print (type(offset))
            query += "OFFSET %s "
            params.append(int(offset))


    print (query)
    return json, query, params



def read_last_test_number(mysql):
    query = "SELECT TestNumber FROM MECPerf.Test ORDER BY ID desc limit 1"
    cur = mysql.connection.cursor()
    cur.execute(query, [])
    query_res = cur.fetchone()
    if len(query_res) == 0:
        cur.close()
        return 1

    cur.close()
    return query_res[0] + 1
    
    

def insert_test_table(cur, actual_test_number, test_type, test_info, direction = "", measure_type = ""):
    insert_Test_table_query_active = "INSERT INTO MECPerf.Test (TestNumber,Timestamp, Direction, Command, " +\
                                                        " SenderIdentity, ReceiverIdentity, " +\
                                                        " SenderIPv4Address, ReceiverIPv4Address, Keyword, " +\
                                                        " PackSize, NumPack) VALUES (%s, CURRENT_TIMESTAMP," +\
                                                        " %s, %s, %s, %s, %s, %s, %s, %s, %s)"
    insert_Test_table_query_passive = "INSERT INTO MECPerf.PassiveTest (Timestamp, ClientIP, ClientPort, "+\
                                                        "ServerIP, ServerPort, Keyword, Direction, "+\
                                                        "Protocol, Mode, Type) VALUES (CURRENT_TIMESTAMP, "+\
                                                        " %s, %s, %s, %s, %s, %s, %s, %s, %s)"

    if test_type == "active":
        cur.execute(insert_Test_table_query_active, [actual_test_number, test_info['Direction'], 
                    test_info['Command'], test_info['SenderIdentity'], test_info['ReceiverIdentity'], 
                    test_info['SenderIPv4Address'], test_info['ReceiverIPv4Address'], test_info['Keyword'], 
                    test_info['PackSize'], test_info['NumPack']])
    if test_type == "passive":
        cur.execute(insert_Test_table_query_passive, [ test_info['client_ip'], test_info['client_port'], 
                    test_info['server_ip'], test_info['server_port'], test_info['service'], 
                    direction, test_info['protocol'], test_info['mode'], measure_type])




def insert_bandwidth_table(cur, rowID, test_type, test_values):
    insert_BandwidthMeasure_table_active = "INSERT INTO MECPerf.BandwidthMeasure VALUES (%s, %s, %s, %s)"
    insert_BandwidthMeasure_table_passive = "INSERT INTO MECPerf.PassiveBandwidthMeasure (ID, Timestamp, bytes) VALUES (%s, %s, %s)"

    if test_type == "active":
        for i in range(0, len(test_values)):
            args = [rowID, i + 1,  int(test_values[i]['nanoTimes']), round(float(test_values[i]['kBytes']), 4)]
                    
            cur.execute(insert_BandwidthMeasure_table_active, args)

    if test_type == "passive":
        for timestamp in test_values:
            args = [rowID, int(timestamp), int(test_values[timestamp])]           
            cur.execute(insert_BandwidthMeasure_table_passive, args)


def insert_latency_table(cur, rowID, test_type, test_values):
    insert_latency_query_active = "INSERT INTO MECPerf.RttMeasure (id, sub_id, latency, timestamp_millis) VALUES (%s, %s, %s, %s)"
    insert_latency_query_passive = "INSERT INTO MECPerf.PassiveRttMeasure (ID, Timestamp, latency) VALUES (%s, %s, %s)"

    if test_type == "active":
        for i in range(0, len(test_values)):
            args = [rowID, int(test_values[i]['sub_id']), float(test_values[i]['latency']), 
                    int(test_values[i]['timestamp_millis'])]
            cur.execute(insert_latency_query_active, args)

    if test_type == "passive":
        for timestamp in test_values:
            args = [rowID, long(timestamp),  float(test_values[timestamp])]
            cur.execute(insert_latency_query_passive, args)


def insert_metadata_table(cur, rowID, test_type, test_metadata, key):
    assert test_type == "active"

    insert_metadata_query_active = "INSERT INTO ExperimentMETADATA (id, experiment_details) VALUES (%s, %s)"
    #print(json.dumps(test_metadata))
    args = [rowID, json.dumps(test_metadata)]

    cur.execute(insert_metadata_query_active, args)
    

def update_bandwidth(test, mysql):
    if test.type == "active":
        return update_active_bandwidth(test, mysql)
    elif test.type == "passive":
        return update_passive_bandwidth(test, mysql)
    else:
        print ("\n\n" + FAIL + "ERROR unknown test.type" + ENDC)
        return 501

                



def update_latencies(test, mysql):
    if test.type == "active":
        return update_active_latencies(test, mysql)
    elif test.type == "passive":
        return update_passive_latencies(test, mysql) 
    else:
        print ("\n\n" + FAIL + "ERROR unknown test.type" + ENDC  )  
        return NOT_IMPLEMENTED


def update_active_bandwidth(test, mysql):
    actual_test_number = read_last_test_number(mysql)
    
    mysql.connection.autocommit = False
    try:
        cur = mysql.connection.cursor()

        #store first segment measures
        insert_test_table(cur, actual_test_number, test.type, test.test_info_first_segment)
        last_rowID = int(cur.lastrowid)
        insert_bandwidth_table(cur, last_rowID, test.type, test.test_values_first_segment)
        if (test.metadata_first_segment != None):
            insert_metadata_table(cur, last_rowID, test.type, test.metadata_first_segment, "metadata_first_segment")

        #update second segment measures
        insert_test_table(cur, actual_test_number, test.type, test.test_info_second_segment)
        last_rowID = int(cur.lastrowid)
        insert_bandwidth_table(cur, last_rowID, test.type, test.test_values_second_segment)
        if (test.metadata_second_segment != None):
            insert_metadata_table(cur, last_rowID, test.type, test.metadata_first_segment, "metadata_second_segment")


        mysql.connection.commit()
        mysql.connection.autocommit=True
        cur.close()
    
    except mysql.connection.Error as e:
        print (test.test_info_first_segment)
        print (test.test_info_second_segment)
        print (FAIL + "Failed to update the database.")
        print (e)
        print ("Execute a roll back" + ENDC)
        mysql.connection.rollback()
        cur.close()

        return INTERNAL_SERVER_ERROR
    
    return OK



def update_active_latencies(test, mysql):
    actual_test_number = read_last_test_number(mysql)
    
    mysql.connection.autocommit = False
    try:
        cur = mysql.connection.cursor()

        #store first segment measures
        insert_test_table(cur, actual_test_number, test.type, test.test_info_first_segment)
        last_rowID = int(cur.lastrowid)
        insert_latency_table(cur, last_rowID , test.type, test.test_values_first_segment)
        if (test.metadata_first_segment != None):
            insert_metadata_table(cur, last_rowID, test.type, test.metadata_first_segment, "metadata_first_segment")

        #update second segment measures
        insert_test_table(cur, actual_test_number, test.type, test.test_info_second_segment)
        last_rowID = int(cur.lastrowid)
        insert_latency_table(cur, last_rowID, test.type, test.test_values_second_segment)
        if (test.metadata_second_segment != None):
            insert_metadata_table(cur, last_rowID, test.type, test.metadata_second_segment, "metadata_second_segment")


        mysql.connection.commit()
        mysql.connection.autocommit=True
        cur.close()
    
    except mysql.connection.Error as e:
        print ("Failed to update the database.")
        print (e)
        print ("Execute a roll back")
        mysql.connection.rollback()
        cur.close()
        return INTERNAL_SERVER_ERROR

    return OK



def update_passive_bandwidth(test, mysql):
    mysql.connection.autocommit = False
    try:
        cur = mysql.connection.cursor()

        if len(test.uplink) != 0:
            insert_test_table(cur, -1, test.type, test.info, 'uplink', 'bandwidth')
            insert_bandwidth_table(cur, cur.lastrowid, test.type, test.uplink)
        if len(test.downlink) != 0:
            insert_test_table(cur, -1, test.type, test.info, 'downlink', 'bandwidth')
            insert_bandwidth_table(cur, cur.lastrowid, test.type, test.downlink)

        mysql.connection.commit()
        mysql.connection.autocommit=True
        cur.close()
    
    except mysql.connection.Error as e:
        print (FAIL + "Failed to update the database.")
        print (e)
        print ("Execute a roll back" + ENDC)
        mysql.connection.rollback()
        cur.close()

        return INTERNAL_SERVER_ERROR
    
    return OK


def update_passive_latencies(test, mysql):
    mysql.connection.autocommit = False
    try:
        cur = mysql.connection.cursor()

        if len(test.uplink) != 0:
            insert_test_table(cur, -1, test.type, test.info, 'uplink', 'latency')
            insert_latency_table(cur, cur.lastrowid, test.type, test.uplink)
        if len(test.downlink) != 0:
            insert_test_table(cur, -1, test.type, test.info, 'downlink', 'latency')
            insert_latency_table(cur, cur.lastrowid, test.type, test.downlink)


        mysql.connection.commit()
        mysql.connection.autocommit=True
        cur.close()
    
    except mysql.connection.Error as e:
        print ("Failed to update the database.")
        print (e)
        print ("Execute a roll back")
        mysql.connection.rollback()
        cur.close()
        return INTERNAL_SERVER_ERROR

    return OK

'''




     