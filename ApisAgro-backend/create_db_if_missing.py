import mysql.connector
from mysql.connector import errorcode

def create_database_if_not_exists(host, user, password, db_name):
    try:
        connection = mysql.connector.connect(
            host=host,
            user=user,
            password=password
        )
        cursor = connection.cursor()

        cursor.execute(f"CREATE DATABASE IF NOT EXISTS {db_name}")
        print(f"✅ Database '{db_name}' is ready.")
        cursor.close()
        connection.close()

    except mysql.connector.Error as err:
        if err.errno == errorcode.ER_ACCESS_DENIED_ERROR:
            print("❌ Access denied: check your username or password")
        else:
            print(f"❌ Failed to create database: {err}")

if __name__ == "__main__":
    create_database_if_not_exists(
        host="localhost",
        user="root",
        password="peter",   # 🔁 change this
        db_name="apisagro"
    )
