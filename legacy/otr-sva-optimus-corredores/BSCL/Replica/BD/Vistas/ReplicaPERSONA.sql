SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPERSONA"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPERSONA" AS

SELECT 
	WST."IdStack" AS "IDSTACK","Action" AS ACTION,
	PER_ID AS TABLE_KEY,
	"PER_ID",--int] NOT NULL,***
	"PRO_ID", --int] NOT NULL,
	"NAC_ID",--char](2) NOT NULL,
	"PAR_SEG_ID",--varchar](10) NOT NULL,
	"EST_ID",--varchar](2) NOT NULL********** per.est_id = '08' BLO | DESQ
	"PER_TIP_DOC", --varchar](10) NOT NULL,
	"PER_NUM_DOC", --varchar](20) NOT NULL,******
	"PER_RUT",--int] NOT NULL,
	"PER_DV",--char](1) NOT NULL,
	"PER_RUTDV", --varchar](11) NOT NULL,**********
	"PER_PATERNO", --varchar](60) NOT NULL,***********
	"PER_MATERNO", --varchar](50) NOT NULL,***********
	"PER_NOMBRES", --varchar](30) NOT NULL,************
	"PER_FEC_NACI", --datetime] NOT NULL,*************
	"PER_SEXO", --char](1) NOT NULL,******************
	"PAR_EST_CIVIL", --varchar](10) NOT NULL,****************  
	"PAR_REG_CONYUGAL", --varchar](10) NOT NULL
	"PER_TIP_PER", --char](3) NOT NULL,****************
	"PER_FEC_INGRESO",--datetime] NOT NULL,************
	"PER_FEC_MODIFICA",--datetime] NOT NULL,
	"PER_RAZ_SOCIAL", --varchar](80) NOT NULL,****************
	"PER_OBS_BLOQUEO", --varchar](250) NOT NULL,
	"PER_CARGO", --varchar](30) NOT NULL,
	"PER_EMPLEADOR", --varchar](100) NOT NULL,***************
	"PER_PAIS_ISO", --char](2) NOT NULL,************
	"PER_COM_ID", --int] NOT NULL,
	"PER_RESIDENCIA",--char](3) NOT NULL,
	"PER_ACTIVIDAD", --varchar](30) NOT NULL,
	"PER_SUC_ID", --char](4) NOT NULL,
	"PER_FECHA_KYC", --datetime] NOT NULL,
	"PER_NOM_CONYUGUE",--varchar](100) NOT NULL,
	"PER_ACTIVIDAD_ID",--int] NOT NULL,
	"PER_SITUAC_LAB",--varchar](10) NOT NULL,en seb
	"PER_DIR_EMPLEADOR",--varchar](100) NOT NULL,*********  S/I optimus
	"PER_TEL_EMPLEADOR",--varchar](20) NOT NULL,*********** S/I optimus
	"PER_ING_ULTANIO_MONEDA",--char](3) NOT NULL,
	"PER_ING_ULTANIO_MONTO",--decimal](24, 4) NOT NULL,
	"PER_ING_ULTANIO_ANIO",--smallint] NOT NULL,
	"PER_IND_PEP",--char](1) NOT NULL,
	"PER_MOTIVO_BLOQUEO",--char](4) NOT NULL,
    "PER_FEC_BLOQUEO",--datetime] NOT NULL,
    "PER_FEC_DESBLOQUEO",--datetime] NOT NULL,
	"PER_TAXID",--varchar](20) NOT NULL,
	"COD_BANCO",--char](10) NOT NULL,
	"PER_FEC_FALLECIMIENTO",--varchar](10) NOT NULL,
	"PER_NO_VALIDA_KYC",--char](1) NOT NULL,
	"PER_NOMBRE_EJECUTIVO",--varchar](60) NOT NULL,
	"PER_NOMBRE_SUCURSAL",--varchar](60) NOT NULL,
	"PER_TIPO_BLOQUEO",--varchar](50) NOT NULL,
	"PER_COD_CLI_ALT",--varchar](50) NOT NULL,
	"PER_ORIGEN_INGRESO",--char](3) NOT NULL,
	"PER_PAIS_RESI",--char](3) NOT NULL,
	"PER_RELA_CORR",--char](10) NOT NULL,
	"PER_PARTI_SOC",--char](1) NOT NULL,
	"PER_SOC_RUT",--varchar](20) NOT NULL,
	"PER_SOC_RAZ_SOCIAL",--varchar](80) NOT NULL,
	"PER_SOC_GIRO",--int] NOT NULL,
	"PER_SOC_PORC_PARTI",--numeric](5, 2) NOT NULL,
	"PER_EMP_RUT",--varchar](20) NOT NULL,
	"PER_EMP_NOM_FANTASIA",--varchar](80) NOT NULL,
	"PER_EMP_GIRO",--varchar](2) NOT NULL, preguntar!
	"PER_GLEI",--varchar](50) NOT NULL,
	"PER_PAIS_ORI_FONDOS",--varchar](3) NOT NULL,
	"PER_PRO_ORI_FONDOS",--varchar](80) NOT NULL,
	"PER_TIP_CLI", --varchar](10) NOT NULL,
	"PER_SUS_CON_INT", --[char](1) NOT NULL,
	"PER_MAN_INFO_PRIV", --[char](1) NOT NULL,
	"PER_RELA_CORR_ART100", --char](1) NOT NULL
	"PER_CLASIF_CLI",--varchar](10) NOT NULL,
	"PAR_ENT_UAF",--varchar](10) NOT NULL,
	"NATURALEZA_ENT_UAF",--varchar](150) NOT NULL,
	"FOLIO_ENT_UAF", --int] NOT NULL,
	"NACIONALIDAD_ENT_UAF",--char](2) NOT NULL,
	"RUT_ENT_UAF",--char](11) NOT NULL,
	"NOMBRE_ENT_UAF",--varchar](200) NOT NULL,
	"RELACION_ENT_UAF",--, --varchar](200) NOT NULL
	"TIPO_NAC_UAF", --] [varchar](10) NOT NULL,
	"TIPO_DECLARACION_UAF" --] [varchar](10) NOT NULL,
FROM "BSCL.Replica.BD.Vistas::Sebra.PERSONA"

LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'PERSONA' AND WST."Key" = PER_ID

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPERSONA" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
