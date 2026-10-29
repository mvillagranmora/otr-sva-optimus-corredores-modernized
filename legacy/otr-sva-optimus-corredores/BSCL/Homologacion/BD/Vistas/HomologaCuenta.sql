SET SCHEMA "BSCL"; 
--GO 
DROP VIEW "BSCL.Homologacion.BD.Vistas::HomologaCuenta";
--GO
create view "BSCL.Homologacion.BD.Vistas::HomologaCuenta" as 
SELECT 
per_id, --int] NOT NULL,  *****************
CUENTA,
fil_id, --int] NOT NULL,
RUT_CLI, --varchar](11) NOT NULL, *****************
est_id, --varchar](2) NOT NULL,   *****************
par_tip_proposito, --varchar](10) NOT NULL, *****************
cue_descripcion, --varchar](50) NOT NULL,
emp_codigo, --char](4) NOT NULL,  *****************
emp_referencia, --char](4) NOT NULL,
rso_id, --int] NOT NULL,
par_tip_dcv, --varchar](10) NOT NULL,
cue_cta_dcv, --char](12) NOT NULL,
tpe_id, --char](2) NOT NULL,
par_tpp_id, --varchar](10) NOT NULL,
par_tpc_id, --varchar](10) NOT NULL,
par_linea_id, --varchar](10) NOT NULL,
par_mfa_id, --varchar](10) NOT NULL,
cue_gas_factura, --char](1) NOT NULL,
cue_fus_facprecio, --char](1) NOT NULL,
cue_sep_comvta, --char](1) NOT NULL,
cue_sep_ins, --char](1) NOT NULL,
cue_sep_palo, --char](1) NOT NULL,
cue_net_carabo, --char](1) NOT NULL,
cue_ult_cartola, --int] NOT NULL,
cue_fec_ultcart, --datetime] NOT NULL,
cue_fec_ingreso, --datetime] NOT NULL,
cue_fec_actualizacion, --datetime] NOT NULL,
cue_fec_ultoper, --datetime] NOT NULL,
cue_obs_bloqueo, --varchar](250) NOT NULL,
suc_id, --char](4) NOT NULL,
cue_cta_dvl, --char](12) NOT NULL,
cue_mancomunada, --char](1) NOT NULL,
cue_alias, --varchar](30) NOT NULL,
cue_motivo_bloqueo, --char](4) NOT NULL,
cue_fec_bloqueo, --datetime] NOT NULL,
cue_fec_desbloqueo, --datetime] NOT NULL,
fecha_contrato_unico, --datetime] NOT NULL,
fecha_contrato_cg, --datetime] NOT NULL,
cue_nro_acuerdo, --varchar](20) NOT NULL,
dias_saldo, --numeric](18, 0) NOT NULL,
cue_fus_facprecio_prom, --char](1) NOT NULL,
cue_res_nemo_fac --char](1) NOT NULL
FROM (
SELECT 
PER."IdPersona"  AS per_id, --int] NOT NULL,  *****************
POR."IdPortafolio" AS CUENTA,
-- RANK() over (partition by  "IdCliente"  order by "IdPortafolio" DESC) as RANK ¿Peligroso?
--CASE WHEN POR."CodigoPortafolio" = '' THEN 0 ELSE to_number(left(POR."CodigoPortafolio",2)) END  AS CUENTA, --int] NOT NULL,  *****************
'' AS fil_id, --int] NOT NULL,
IDENT."DocumentoIdentidad" AS RUT_CLI, --varchar](11) NOT NULL, *****************
CASE WHEN POR."IdEstado" = 'ACT' then '15' ELSE '14' END AS est_id, --varchar](2) NOT NULL,   *****************
left(POR."Proposito",10) AS par_tip_proposito, --varchar](10) NOT NULL, *****************
'' AS cue_descripcion, --varchar](50) NOT NULL,
left(coalesce(EMP."CodigoEmpleado",''),4) AS emp_codigo, --char](4) NOT NULL,  *****************
'' AS emp_referencia, --char](4) NOT NULL,
'' AS rso_id, --int] NOT NULL,
'' AS par_tip_dcv, --varchar](10) NOT NULL,
'' AS cue_cta_dcv, --char](12) NOT NULL,
'' AS tpe_id, --char](2) NOT NULL,
'' AS par_tpp_id, --varchar](10) NOT NULL,
'' AS par_tpc_id, --varchar](10) NOT NULL,
'' AS par_linea_id, --varchar](10) NOT NULL,
'' AS par_mfa_id, --varchar](10) NOT NULL,
'' AS cue_gas_factura, --char](1) NOT NULL,
'' AS cue_fus_facprecio, --char](1) NOT NULL,
'' AS cue_sep_comvta, --char](1) NOT NULL,
'' AS cue_sep_ins, --char](1) NOT NULL,
'' AS cue_sep_palo, --char](1) NOT NULL,
'' AS cue_net_carabo, --char](1) NOT NULL,
'' AS cue_ult_cartola, --int] NOT NULL,
'' AS cue_fec_ultcart, --datetime] NOT NULL,
'' AS cue_fec_ingreso, --datetime] NOT NULL,
'' AS cue_fec_actualizacion, --datetime] NOT NULL,
'' AS cue_fec_ultoper, --datetime] NOT NULL,
'' AS cue_obs_bloqueo, --varchar](250) NOT NULL,
'' AS suc_id, --char](4) NOT NULL,
'' AS cue_cta_dvl, --char](12) NOT NULL,
'' AS cue_mancomunada, --char](1) NOT NULL,
'' AS cue_alias, --varchar](30) NOT NULL,
'' AS cue_motivo_bloqueo, --char](4) NOT NULL,
'' AS cue_fec_bloqueo, --datetime] NOT NULL,
'' AS cue_fec_desbloqueo, --datetime] NOT NULL,
'' AS fecha_contrato_unico, --datetime] NOT NULL,
'' AS fecha_contrato_cg, --datetime] NOT NULL,
'' AS cue_nro_acuerdo, --varchar](20) NOT NULL,
'' AS dias_saldo, --numeric](18, 0) NOT NULL,
'' AS cue_fus_facprecio_prom, --char](1) NOT NULL,
'' AS cue_res_nemo_fac, --char](1) NOT NULL
RANK() over (partition by  POR."IdCliente"  order by POR."IdPortafolio" DESC) as CUENTA_RANK
from "BSCL.SocioNegocio.BD::PER.Persona" as PER
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" as CLI ON CLI."IdPersona" = PER."IdPersona"
INNER JOIN  "BSCL.Portafolio.BD::POR.Portafolio"  as POR ON CLI."IdCliente" = POR."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::EJE.Ejecutivo" as EJE ON EJE."IdEmpleadoEjecutivo" = POR."IdEmpleadoEjecutivo"
LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" as EMP ON EJE."IdEmpleadoEjecutivo" = EMP."IdEmpleado")
;
--GO
GRANT SELECT ON "BSCL.Homologacion.BD.Vistas::HomologaCuenta" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
--GO