CREATE DATABASE [cm_management]
GO
ALTER DATABASE [cm_management] SET COMPATIBILITY_LEVEL = 100
GO
ALTER DATABASE [cm_management] SET ANSI_NULL_DEFAULT OFF
GO
ALTER DATABASE [cm_management] SET ANSI_NULLS OFF
GO
ALTER DATABASE [cm_management] SET ANSI_PADDING OFF
GO
ALTER DATABASE [cm_management] SET ANSI_WARNINGS OFF
GO
ALTER DATABASE [cm_management] SET ARITHABORT OFF
GO
ALTER DATABASE [cm_management] SET AUTO_CLOSE OFF
GO
ALTER DATABASE [cm_management] SET AUTO_CREATE_STATISTICS ON
GO
ALTER DATABASE [cm_management] SET AUTO_SHRINK OFF
GO
ALTER DATABASE [cm_management] SET AUTO_UPDATE_STATISTICS ON
GO
ALTER DATABASE [cm_management] SET CURSOR_CLOSE_ON_COMMIT OFF
GO
ALTER DATABASE [cm_management] SET CURSOR_DEFAULT GLOBAL
GO
ALTER DATABASE [cm_management] SET CONCAT_NULL_YIELDS_NULL OFF
GO
ALTER DATABASE [cm_management] SET NUMERIC_ROUNDABORT OFF
GO
ALTER DATABASE [cm_management] SET QUOTED_IDENTIFIER OFF
GO
ALTER DATABASE [cm_management] SET RECURSIVE_TRIGGERS OFF
GO
ALTER DATABASE [cm_management] SET DISABLE_BROKER
GO
ALTER DATABASE [cm_management] SET AUTO_UPDATE_STATISTICS_ASYNC OFF
GO
ALTER DATABASE [cm_management] SET DATE_CORRELATION_OPTIMIZATION OFF
GO
ALTER DATABASE [cm_management] SET PARAMETERIZATION SIMPLE
GO
ALTER DATABASE [cm_management] SET READ_WRITE
GO
ALTER DATABASE [cm_management] SET RECOVERY FULL
GO
ALTER DATABASE [cm_management] SET MULTI_USER
GO
ALTER DATABASE [cm_management] SET PAGE_VERIFY CHECKSUM
GO
USE [cm_management]
GO
IF NOT EXISTS (SELECT name FROM sys.filegroups WHERE is_default=1 AND name = N'PRIMARY') ALTER DATABASE [cm_management] MODIFY FILEGROUP [PRIMARY] DEFAULT
GO

USE [master]
GO
CREATE LOGIN [cm_management] WITH PASSWORD=N'cm_management', DEFAULT_DATABASE=[cm_management], CHECK_EXPIRATION=OFF, CHECK_POLICY=OFF
GO
USE [cm_management]
GO
CREATE USER [cm_management] FOR LOGIN [cm_management]
GO
USE [cm_management]
GO
ALTER USER [cm_management] WITH DEFAULT_SCHEMA=[cm_management]
GO
USE [cm_management]
GO
EXEC sp_addrolemember N'db_datareader', N'cm_management'
GO
USE [cm_management]
GO
EXEC sp_addrolemember N'db_datawriter', N'cm_management'
GO
USE [cm_management]
GO
EXEC sp_addrolemember N'db_ddladmin', N'cm_management'
GO

USE [cm_management]
GO
CREATE SCHEMA [cm_management] AUTHORIZATION [cm_management]
GO
USE [cm_management]
GO
ALTER USER [cm_management] WITH DEFAULT_SCHEMA=[cm_management]
GO
