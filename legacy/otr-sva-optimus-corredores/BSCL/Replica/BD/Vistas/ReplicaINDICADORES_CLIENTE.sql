SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_CLIENTE";
--select * from "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_CLIENTE" where table_key = '9701800041'
--select * from "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion"

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_CLIENTE" AS

	SELECT 
			"IdStack"  AS "IDSTACK" , "Action" AS ACTION,
			(PER_ID || IND_ID) AS TABLE_KEY,
			"PER_ID", --[per_id] [int] NOT NULL,
			"FIL_ID", --[fil_id] [int] NOT NULL,
			"IND_ID", --[ind_id] [int] NOT NULL,
			"DIN_ID", --[din_id] [char](1) NOT NULL,
			"IND_FEC_INICIO"--, --[ind_fec_inicio] [datetime] NOT NULL,
			--"IND_FEC_SUSCRIPCION", --[ind_fec_suscripcion] [datetime] NULL,
			--"IND_FOLIO" --[ind_folio] [varchar](250) NULL,
	
		FROM "BSCL.Replica.BD.Vistas::Sebra.INDICADORES_CLIENTE" i
		left JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion"   AS s
					ON (s."Table" ='INDICADORES_CLIENTE' AND (PER_ID || IND_ID) = s."Key")
		
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_CLIENTE" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

