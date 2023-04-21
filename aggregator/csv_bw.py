# insert the name of the file CSV
csv_file = "measure_bw.csv"

# insert the id of the test --> you need to make the query in the DB
target_value = input("Insert the id of the test: ")

# open file CSV
with open(csv_file, 'r') as file:
    # read the file row for row
    nanoTimes = []
    kBytes = 0
    for line in file:
        # clean each row from spaces and so on and divide the row in single values 
        row = line.rstrip().split(',')
        # take just the id necessary
        if row[0] == target_value:
            # compute bandwidth
            nanoTime = float(row[2])
            kByte = float(row[3])
            nanoTimes.append(nanoTime)
            kBytes += kByte
    dt = nanoTimes[-1]-nanoTimes[0]
    print(f"{kBytes}/{dt}")

    bw = kBytes/dt*10 
    # I'm using just a factor 10 because: ns is 10^(-9), but in the aggregator I introduced a product
    # the product is between the value of kBytes and 10^(8)
    # So, to convert the bandwidth in kByte/s, I should multiply for 10^(9) and divide for 10^(-8)
    print("For the test with ID: ", target_value, "the estimated bandwidth2 is: ", bw, "kByte/s")

