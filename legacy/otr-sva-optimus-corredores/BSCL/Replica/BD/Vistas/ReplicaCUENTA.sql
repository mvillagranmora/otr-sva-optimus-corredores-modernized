SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaCUENTA";

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaCUENTA" AS
SELECT 
	WST."IdStack"  AS "IDSTACK" , 
	"Action" AS ACTION,
	"IdPortafolio" AS TABLE_KEY,
	per_id, --int] NOT NULL,  *****************	
	CUENTA,
	fil_id, --int] NOT NULL,
	RUT_CLI, --varchar](11) NOT NULL, *****************
	est_id, --varchar](2) NOT NULL,   *****************
	par_tip_proposito, --varchar](10) NOT NULL, *****************
	cue_descripcion, --varchar](50) NOT NULL,
	emp_codigo, --char](4) NOT NULL,  *****************
	emp_referencia, -- [char](4) NOT NULL,
	rso_id, -- [int] NOT NULL,
	par_tip_dcv, -- [varchar](10) NOT NULL,
	cue_cta_dcv, -- [char](12) NOT NULL,
	tpe_id, -- [char](2) NOT NULL,
	par_tpp_id, -- [varchar](10) NOT NULL,
	par_tpc_id, -- [varchar](10) NOT NULL,
	par_linea_id, -- [varchar](10) NOT NULL,
	par_mfa_id, -- [varchar](10) NOT NULL,
	cue_gas_factura, -- [char](1) NOT NULL,
	cue_fus_facprecio, -- [char](1) NOT NULL,
	cue_sep_comvta, -- [char](1) NOT NULL,
	cue_sep_ins, -- [char](1) NOT NULL,
	cue_sep_palo, -- [char](1) NOT NULL,
	cue_net_carabo, -- [char](1) NOT NULL,
	cue_ult_cartola, -- [int] NOT NULL,
	cue_fec_ultcart, -- [datetime] NOT NULL,
	"CUE_FEC_INGRESO", --datetime] NOT NULL,*************
	cue_fec_actualizacion, --datetime] NOT NULL,*************
	cue_fec_ultoper, -- [datetime] NOT NULL,
	cue_obs_bloqueo, -- [varchar](250) NOT NULL,
	suc_id, -- [char](4) NOT NULL,
	cue_cta_dvl, -- [char](12) NOT NULL,
	cue_mancomunada, -- [char](1) NOT NULL,
	cue_alias, -- [varchar](30) NULL,
	cue_motivo_bloqueo, -- [char](4) NOT NULL,
	cue_fec_bloqueo, --datetime] NOT NULL,*************
	cue_fec_desbloqueo, --datetime] NOT NULL,*************
	fecha_contrato_unico, -- [datetime] NOT NULL,
	fecha_contrato_cg, -- [datetime] NOT NULL,
	cue_nro_acuerdo, -- [varchar](20) NOT NULL,
	dias_saldo, -- [numeric](18, 0) NOT NULL,
	cue_period_cartola, -- [char](1) NOT NULL,
	cue_cartola_impresa, -- [char](1) NOT NULL,
	cue_con_cartola, -- [char](1) NOT NULL,
	cue_fus_facprecio_prom, -- [char](1) NOT NULL,
	cue_res_nemo_fac, -- [char](1) NOT NULL,}
	cua_fac_simp -- [char](1) NOT NULL
FROM "BSCL.Replica.BD.Vistas::Sebra.CUENTA" CUE
LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'CUENTA' AND WST."Key" = CUE."IdPortafolio"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaCUENTA" TO  BCS_DESARROLLADOR WITH GRANT OPTION;


