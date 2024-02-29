# CSV JDBC Connector

This connector - a JDBC Interface Type - facilitates data access from CSV files, treating them as table-based sources. Once connected, you can explore these CSV files within the DB Explorer, construct queries, and utilize them in the studio for tasks such as schema building or Broadway actors.

When creating an inteface build upon this type, specifiy the folder containing the CSV files in the DB Host field.

> **Notes**:
> * You can include multiple CSV files within the designated folder. Each file will be represented as an individual table within this data source. 
> * The connector accepts only SQL _SELECT_ queries and does not accommodate _INSERT_, _UPDATE_, _DELETE_ or _CREATE_ statements.
> * The connector accepts only queries from a single table and does not support _JOIN_ statments.

Additionally, you have the flexibility to set various parameters, including the separator (with the default being a comma).

JDBC Driver Source Code can be found [here](https://github.com/simoc/csvjdbc).

Full documentation for the CSV JDBC driver can be found [here](https://github.com/simoc/csvjdbc/blob/master/docs/doc.md).
