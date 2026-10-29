SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaDIRECCION"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaDIRECCION" AS
	SELECT 
		WST."IdStack"  AS "IDSTACK" , "Action" AS ACTION,
		DIR_ID AS TABLE_KEY,
		"DIR_ID",--int] NOT NULL,
		"COM_ID",--int] NOT NULL, ***
		"PAR_TDI_ID",--varchar](10) NOT NULL,***
		"PER_ID",--int] NOT NULL,***
		"DIR_NOMBRE",--varchar](50) NOT NULL,***
		"DIR_NUMERO",--varchar](15) NOT NULL,***
		"DIR_COMPLEMENTO",--varchar](50) NOT NULL,***
		"DIR_COD_POSTAL",--varchar](11) NOT NULL,
		"DIR_PRESENTACION",--varchar](80) NOT NULL,***
		"DIR_INVALIDA",--char](1) NOT NULL,
		"DIR_FEC_MODIFICA",--datetime] NOT NULL,
		"DIR_BARRIO",--varchar](30) NOT NULL,
		"DIR_CONTACTO",--varchar](50) NOT NULL,
		"DIR_MODIFICABLE",--char](1) NOT NULL,
		"DIR_IND_PRINCIPAL"--char](1) NOT NULL,
	FROM "BSCL.Replica.BD.Vistas::Sebra.DIRECCION" DIR
	LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'DIRECCION' AND WST."Key" = DIR."DIR_ID"



WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaDIRECCION" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

