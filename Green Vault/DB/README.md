# Database Folder

This folder contains the H2 database files for the GreenVault application.

## Database Files

- **greenvault.mv.db** - Main database file (created automatically)
- **greenvault.trace.db** - Database trace log (created automatically if tracing is enabled)

## Location

The database is stored at: `./database/greenvault`

## JDBC Connection

- **Embedded Mode**: `jdbc:h2:file:./database/greenvault`
- **Server Mode**: `jdbc:h2:tcp://localhost:9092/./database/greenvault`

## Notes

- The database folder is created automatically when the application starts
- Database files are persistent and will remain after application shutdown
- To reset the database, delete the `greenvault.mv.db` file and run `init-database.bat`

