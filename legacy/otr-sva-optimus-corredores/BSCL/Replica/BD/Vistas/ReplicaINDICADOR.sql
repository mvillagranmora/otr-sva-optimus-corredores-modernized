SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaINDICADOR"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaINDICADOR" AS
		

SELECT 
	"IdStack"  AS "IDSTACK" , "Action" AS ACTION,
	 cast("IND_ID" AS varchar) AS TABLE_KEY,
	"IND_ID", --[ind_id] [int] NOT NULL,
	"IND_TIPO", --[ind_tipo] [char](2) NOT NULL,
	"IND_AMBITO", --[ind_ambito] [char](3) NOT NULL,
	"IND_NOMBRE", --[ind_nombre] [varchar](50) NOT NULL,
	"IND_TIP_PER", --[ind_tip_per] [char](3) NOT NULL,
	"IND_EXPIRA", --[ind_expira] [char](1) NOT NULL,
	"IND_DURACION_EXPIRA", --[ind_duracion_expira] [int] NOT NULL,
	"IND_TIPO_OPERADOR", --[ind_tipo_operador] [char](1) NOT NULL,
	"IND_IND_REQUISITO", --[ind_ind_requisito] [char](1) NOT NULL,
	"REG_ID", --[reg_id] [char](5) NOT NULL,
	"IND_TAB_HOMOLOG", --[ind_tab_homolog] [varchar](8) NOT NULL,
	"IND_CAM_HOMOLOG", --[ind_cam_homolog] [varchar](20) NOT NULL,
	"IND_POS_HOMOLOG", --[ind_pos_homolog] [int] NOT NULL,
	"IND_POS_PARAMETRO", --, --[ind_pos_parametro] [int] NOT NULL,
	"IND_FOLIO", --[ind_folio] [varchar](50) NULL,
	"IND_FECHA_SUSCRIPCION"--[ind_fecha_suscripcion] [varchar](50) NULL,


   	FROM "BSCL.Replica.BD.Vistas::Sebra.INDICADOR" AS i
	LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion"   AS s
			ON (s."Table" ='INDICADOR' AND cast(i."IND_ID" AS varchar(200)) = s."Key")
				
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaINDICADOR" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
