SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.CUENTA"; 
--SELECT * FROM "BSCL.Replica.BD.Vistas::Sebra.CUENTA" WHERe "IdPortafolio" = 7081
--SELECT * FROM BSCL."BSCL.Replica.BD.Vistas::Sebra.CUENTA" WHERE (("PER_ID" >= 22133808 and "PER_ID" <= 22133808))

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.CUENTA" AS

SELECT 
	CAST(
		CASE 
			WHEN instr(IDENT."DocumentoIdentidad" , '-') = 0 THEN -1 
			ELSE trim(LEFT(IDENT."DocumentoIdentidad"  , instr(IDENT."DocumentoIdentidad" , '-')-1)) END AS INT) AS per_id, --int] NOT NULL,  *****************
	TO_INT(POR."CodigoPortafolio") AS CUENTA,
	0 AS fil_id, --int] NOT NULL,
	coalesce ("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"("DocumentoIdentidad"), '')  AS RUT_CLI, --varchar](11) NOT NULL, *****************
	CAST(CASE WHEN POR."IdEstado" = 'ACT' then '15' ELSE '14' END AS VARCHAR(2)) AS est_id, --varchar](2) NOT NULL,   *****************
	LEFT(COALESCE( hpr."ValorOrigen", POR."Proposito"),10) AS par_tip_proposito, --varchar](10) NOT NULL, *****************
	LEFT(coalesce (POR."DescripcionPortafolio", ''), 50) AS cue_descripcion, --varchar](50) NOT NULL,
	CAST(coalesce(COD_EMP_TRUN4."CodigoEmpleado",'') AS VARCHAR(4)) AS emp_codigo, --char](4) NOT NULL,  *****************
	CAST(coalesce(COD_EMP_COM_TRUN4."CodigoEmpleado",'') AS VARCHAR(4)) AS emp_referencia, -- [char](4) NOT NULL,
	TO_INT(CASE WHEN NF."IdNombreFantasia" IS NOT NULL THEN NF."IdNombreFantasia"
		 WHEN coalesce(PER."IdNombreFantasiaPrincipal", 0) = 0 THEN PER."IdPersona" * -1 
		 ELSE PER."IdNombreFantasiaPrincipal" END) AS rso_id, -- [int] NOT NULL,
	LEFT(coalesce(CASE WHEN CUENTA_C_PORTAFOLIO."IdTipoCuentaCustodia" = '' THEN 'TCDCVDEPOS' 
	                   WHEN CUENTA_C_PORTAFOLIO."IdTipoCuentaCustodia" = 'DEPOSITANTE' THEN 'TCDCVDEPOS' 
	                   WHEN CUENTA_C_PORTAFOLIO."IdTipoCuentaCustodia" = 'MANDANTE' THEN 'TCDCVMANDA'
	                   ELSE CUENTA_C_PORTAFOLIO."IdTipoCuentaCustodia" END,'TCDCVNINGU'),10) AS par_tip_dcv, -- [varchar](10) NOT NULL,
	LEFT(coalesce(CUENTA_C_PORTAFOLIO."Codigo", '0'),12) AS cue_cta_dcv, -- [char](12) NOT NULL,
	COALESCE(TPE.TPE_ID, '') AS tpe_id, -- [char](2) NOT NULL,
	TRIM(LEFT(coalesce (POR."IdPerfilProductos", ''), 10)) AS par_tpp_id, -- [varchar](10) NOT NULL,
	TRIM(LEFT(coalesce (POR."IdPerfilCobros", ''), 10)) AS par_tpc_id, -- [varchar](10) NOT NULL,
	'DESCONOCID' AS par_linea_id, -- [varchar](10) NOT NULL,
	'DESCONOCID' AS par_mfa_id, -- [varchar](10) NOT NULL,
	'S' AS cue_gas_factura, -- [char](1) NOT NULL Sebra y en Optimus siempre se incluyen los gastos en facturas,
	COALESCE(i_fus_facprecio."ValorIndicador", 'N') AS cue_fus_facprecio, -- [char](1) NOT NULL,
	'S' AS cue_sep_comvta, -- [char](1) NOT NULL,
	COALESCE(i_sep_ins."ValorIndicador", 'N') AS cue_sep_ins, -- [char](1) NOT NULL,
	COALESCE(i_sep_palo."ValorIndicador", 'N') AS cue_sep_palo, -- [char](1) NOT NULL,
	'N' AS cue_net_carabo, -- [char](1) NOT NULL,
	0 AS cue_ult_cartola, -- [int] NOT NULL,
	CURRENT_TIMESTAMP AS cue_fec_ultcart, -- [datetime] NOT NULL,
	CASE WHEN POR."FechaIngreso" < '1900-01-01' THEN '1900-01-01' ELSE POR."FechaIngreso" END AS "CUE_FEC_INGRESO", --datetime] NOT NULL,*************
	CASE WHEN POR."FechaModificacion" < '1900-01-01' THEN '1900-01-01' ELSE POR."FechaModificacion" END AS cue_fec_actualizacion, --datetime] NOT NULL,*************
	CURRENT_TIMESTAMP AS cue_fec_ultoper, -- [datetime] NOT NULL,
	coalesce (LEFT(POR."ComentarioBloqueo", 250), '') AS cue_obs_bloqueo, -- [varchar](250) NOT NULL,
	COALESCE (SUC."CodigoSucursal",'0') AS suc_id, -- [char](4) NOT NULL,
	'0' AS cue_cta_dvl, -- [char](12) NOT NULL,
	'N' AS cue_mancomunada, -- [char](1) NOT NULL,
	left(coalesce(POR."Alias",''),30) AS cue_alias, -- [varchar](30) NULL,
	coalesce (CAST(POR."IdTipoBloqueo"AS VARCHAR(4)), '')  AS cue_motivo_bloqueo, -- [char](4) NOT NULL,
	CASE WHEN POR."FechaBloqueo" < '1900-01-01' THEN '1900-01-01' ELSE POR."FechaBloqueo" END AS cue_fec_bloqueo, --datetime] NOT NULL,*************
	CASE WHEN POR."FechaDesbloqueo" < '1900-01-01' THEN '1900-01-01' ELSE POR."FechaDesbloqueo" END AS cue_fec_desbloqueo, --datetime] NOT NULL,*************
	CURRENT_TIMESTAMP AS fecha_contrato_unico, -- [datetime] NOT NULL,
	CURRENT_TIMESTAMP AS fecha_contrato_cg, -- [datetime] NOT NULL,
	'' AS cue_nro_acuerdo, -- [varchar](20) NOT NULL,
	TO_BIGINT(0) AS dias_saldo, -- [numeric](18, 0) NOT NULL,
	'' AS cue_period_cartola, -- [char](1) NOT NULL,
	'' AS cue_cartola_impresa, -- [char](1) NOT NULL,
	'' AS cue_con_cartola, -- [char](1) NOT NULL,
	'N' AS cue_fus_facprecio_prom, -- [char](1) NOT NULL,
	'N' AS cue_res_nemo_fac, -- [char](1) NOT NULL,
	'N' AS cue_fac_simp, -- [char](1) NOT NULL
	'N' AS cua_fac_simp, -- [char](1) NOT NULL
	POR."IdPortafolio"
FROM "BSCL.SocioNegocio.BD::PER.Persona" as PER
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" as CLI ON CLI."IdPersona" = PER."IdPersona"
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio"  as POR ON CLI."IdCliente" = POR."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.NombreFantasia" AS NF ON POR."IdNombreFantasia" = NF."IdNombreFantasia"
LEFT JOIN "BSCL.Plataforma.BD::COM.PerfilComercialBase" PCO ON PCO."IdPerfilComercialBase" = POR."IdPerfilComercialBaseOrigen"
LEFT JOIN "BSCL.Replica.BD.Vistas::Sebra.TIPO_PERFIL_COMERCIAL" TPE ON COALESCE (CAST(PCO."IdPerfilComercialBase" AS VARCHAR(30)),'') = TPE."IdPerfilComercialBase"
LEFT JOIN "BSCL.SocioNegocio.BD::EJE.Ejecutivo" as EJE ON EJE."IdEmpleadoEjecutivo" = POR."IdEmpleadoEjecutivo"
LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" as EMP ON EJE."IdEmpleadoEjecutivo" = EMP."IdEmpleado"
LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP_COM ON POR."IdEmpleadoEjecutivoComercial" = EMP_COM."IdEmpleado"
LEFT JOIN (
	SELECT "IdEmpleado", CASE WHEN RANK > 1 THEN CONCAT(RPAD("CodigoEmpleado",3),RANK) ELSE "CodigoEmpleado" END AS "CodigoEmpleado" FROM (
		SELECT RANK() OVER (PARTITION BY RPAD("CodigoEmpleado",4) ORDER BY "CodigoEmpleado" ASC)  as RANK, "IdEmpleado", RPAD("CodigoEmpleado",4) AS "CodigoEmpleado" FROM "BSCL.SocioNegocio.BD::EMP.Empleado"
	) WHERE RANK < 10) AS COD_EMP_TRUN4 ON COD_EMP_TRUN4."IdEmpleado" = EMP."IdEmpleado"
LEFT JOIN (
	SELECT "IdEmpleado", CASE WHEN RANK > 1 THEN CONCAT(RPAD("CodigoEmpleado",3),RANK) ELSE "CodigoEmpleado" END AS "CodigoEmpleado" FROM (
		SELECT RANK() OVER (PARTITION BY RPAD("CodigoEmpleado",4) ORDER BY "CodigoEmpleado" ASC)  as RANK, "IdEmpleado", RPAD("CodigoEmpleado",4) AS 	"CodigoEmpleado" FROM "BSCL.SocioNegocio.BD::EMP.Empleado"
	) WHERE RANK < 10) AS COD_EMP_COM_TRUN4 ON COD_EMP_COM_TRUN4."IdEmpleado" = EMP_COM."IdEmpleado"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" i_sep_palo ON i_sep_palo."CodigoTipoIndicador" = 'FAC_SLFPTR' AND i_sep_palo."IdPortafolio" = POR."IdPortafolio"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" i_fus_facprecio ON i_fus_facprecio."CodigoTipoIndicador" = 'FAC_FLFPPR' AND i_fus_facprecio."IdPortafolio" = POR."IdPortafolio"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" i_sep_ins ON i_sep_ins."CodigoTipoIndicador" = 'FAC_SFPINS' AND i_sep_ins."IdPortafolio" = POR."IdPortafolio"
LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS hpr ON (hpr."TipoValor" = 'PropositoCuenta' 
                                                                      AND hpr."Entidad" = 'PORTAFOLIO' 
                                                                      AND hpr."ValorDestino" = POR."Proposito")
LEFT JOIN (
	SELECT "IdPortafolio","IdTipoCuentaCustodia","Codigo" ,
	row_number() over (partition by "IdPortafolio" order by "IdTipoCuentaCustodia","Codigo") as r
	FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" 
	WHERE "IdEstado" = 'ACT' AND "IdBoveda" = 'DCV' ) as CUENTA_C_PORTAFOLIO
	 ON (POR."IdPortafolio" = CUENTA_C_PORTAFOLIO."IdPortafolio") and r = 1
LEFT JOIN BSCL."BSCL.Plataforma.BD::PLA.Sucursal" AS SUC ON SUC."IdSucursal"= CLI."IdSucursalPreferencia" 
WHERE CAST(
		CASE 
			WHEN instr(IDENT."DocumentoIdentidad" , '-') = 0 THEN -1 
			ELSE trim(LEFT(IDENT."DocumentoIdentidad"  , instr(IDENT."DocumentoIdentidad" , '-')-1)) END AS INT) != -1
	AND IDENT."DocumentoIdentidad" not like '%.%'
	AND "BSCL.Replica.BD.Servicios::func_IsNumeric"(POR."CodigoPortafolio") != 0
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.CUENTA" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
