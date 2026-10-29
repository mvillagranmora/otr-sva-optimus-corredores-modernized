SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaCUENTA_BANCARIA"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaCUENTA_BANCARIA" AS

SELECT
	WST."IdStack"  AS "IDSTACK" , 
	"Action" AS ACTION,
	CBA_ID AS TABLE_KEY,
	"CBA_ID", --int] NOT NULL,
	"PER_ID",--int] NOT NULL,***
	"MON_ID", --char](3) NOT NULL,
	"BAN_ID", --varchar](4) NOT NULL,***********
	"PAR_TCU_ID", --varchar](10) NOT NULL,
	"PAI_ISO", --char](2) NOT NULL,
	"CBA_SUCURSAL", --varchar](30) NOT NULL,
	"CBA_CUENTA", --varchar](20) NOT NULL,*************
	"CBA_BENEFICIARIO", --varchar](50) NOT NULL,
	"CBA_IND_PPAL", --char](1) NOT NULL,
	"CBA_IND_BENEFICIARIO", --char](1) NOT NULL,
	"CTA_BEN_TIP_DOC", --varchar](10) NOT NULL,
	"CTA_BEN_NUM_DOC", --varchar](20) NOT NULL,
	"CBA_IND_DEFAULT", --char](1) NOT NULL,
	"CBA_ORD_PERMANENTE", --char](1) NOT NULL,
	"CBA_GRUPO", --varchar](20) NOT NULL,
	"CBA_DEPOSITOENESTACTA" --char](1) NOT NULL,
FROM "BSCL.Replica.BD.Vistas::Sebra.CUENTA_BANCARIA" CTA
left JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'CUENTA_BANCARIA' AND WST."Key" = CTA."CBA_ID"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaCUENTA_BANCARIA" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

 
