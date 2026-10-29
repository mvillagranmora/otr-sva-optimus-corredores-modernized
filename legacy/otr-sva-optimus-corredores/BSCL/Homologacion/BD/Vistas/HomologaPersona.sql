SET SCHEMA "BSCL"; 
--GO 
DROP VIEW "BSCL.Homologacion.BD.Vistas::HomologaPersona";
--GO
create view "BSCL.Homologacion.BD.Vistas::HomologaPersona" as
SELECT
 PER."IdPersona" AS per_id,--int] NOT NULL,********
'' AS pro_id,--int] NOT NULL,
PAIS."CodigoPais" AS nac_id,--char](2) NOT NULL,
'DESCONOCID' AS par_seg_id,--varchar](10) NOT NULL,
CASE WHEN PER."IdEstado" = 'ACT' then '07' ELSE '08' END  AS est_id,--varchar](2) NOT NULL********** per.est_id = '08' BLO | DESQ
'RUT' AS per_tip_doc,--varchar](10) NOT NULL,
IDENT."DocumentoIdentidad" AS per_num_doc,--varchar](20) NOT NULL,******
to_number(left(IDENT."DocumentoIdentidad",length(IDENT."DocumentoIdentidad")-2)) AS per_rut,--int] NOT NULL,
right(IDENT."DocumentoIdentidad",1) AS per_dv,--char](1) NOT NULL,
IDENT."DocumentoIdentidad" AS per_rutdv,--varchar](11) NOT NULL,**********
CASE WHEN PER."Tipo" = 'N' then PER."ApellidoPaterno" ELSE  PER."RazonSocial" END AS per_paterno,--varchar](60) NOT NULL,***********
CASE WHEN PER."Tipo" = 'N' then PER."ApellidoMaterno" ELSE  PER."RazonSocial" END AS per_materno,--varchar](50) NOT NULL,***********
CASE WHEN PER."Tipo" = 'N' then PER."Nombres" ELSE  PER."RazonSocial" END AS per_nombres,--varchar](30) NOT NULL,************
PER."FechaNacimiento" AS per_fec_naci,--datetime] NOT NULL,*************
PER."Sexo" AS per_sexo,--char](1) NOT NULL,******************
CASE WHEN PER."Tipo" = 'N' AND PER."EstadoCivil" = 'CA' then 'ECIVICASAD'
 WHEN PER."Tipo" = 'N' AND PER."EstadoCivil" = 'DI' then 'ECIVIDIVOR'
 WHEN PER."Tipo" = 'N' AND PER."EstadoCivil" = 'SE' then 'ECIVISEPAR'
 WHEN PER."Tipo" = 'N' AND PER."EstadoCivil" = 'SO' then 'ECIVISOLTE'
 WHEN PER."Tipo" = 'N' AND PER."EstadoCivil" = 'VI' then 'ECIVIVIUDO'
 ELSE 'DESCONOCID' END AS par_est_civil,--varchar](10) NOT NULL,****************  
CASE WHEN PER."Tipo" = 'N' AND PER."RegimenConyugal" = 'PG' then 'RCONYPARTG'
 WHEN PER."Tipo" = 'N' AND PER."RegimenConyugal" = 'SB' then 'RCONYSEPBI'
 WHEN PER."Tipo" = 'N' AND PER."RegimenConyugal" = 'SC' then 'RCONYSOCCO'
 ELSE 'DESCONOCID' END AS par_reg_conyugal,--varchar](10) NOT NULL
CASE WHEN PER."Tipo" = 'N' then 'NAT' ELSE  'JUR' END AS per_tip_per,--char](3) NOT NULL,****************
PER."FechaIngreso" AS per_fec_ingreso,--datetime] NOT NULL,************
PER."FechaModificacion" AS per_fec_modifica,--datetime] NOT NULL,
CASE WHEN PER."Tipo" = 'N' then PER."ApellidoPaterno"||' '||PER."ApellidoMaterno"||' '||PER."Nombres" ELSE  PER."RazonSocial" END   AS per_raz_social,--varchar](80) NOT NULL,****************
'' AS per_obs_bloqueo,--varchar](250) NOT NULL,
'' AS per_cargo,--varchar](30) NOT NULL,
PER."Empleador" AS per_empleador,--varchar](100) NOT NULL,***************
PAIS."CodigoPais" AS per_pais_iso,--char](2) NOT NULL,************
'' AS per_com_id,--int] NOT NULL,
'' AS per_residencia,--char](3) NOT NULL,
'' AS per_actividad,--varchar](30) NOT NULL,
'' AS per_suc_id,--char](4) NOT NULL,
'' AS per_fecha_kyc,--datetime] NOT NULL,
'' AS per_nom_conyugue,--varchar](100) NOT NULL,
'' AS per_actividad_id,--int] NOT NULL,
'' AS per_situac_lab,--varchar](10) NOT NULL,en seb
'' AS per_dir_empleador,--varchar](100) NOT NULL,*********  S/I optimus
'' AS per_tel_empleador,--varchar](20) NOT NULL,*********** S/I optimus
'' AS per_ing_ultanio_moneda,--char](3) NOT NULL,
'' AS per_ing_ultanio_monto,--decimal](24, 4) NOT NULL,
'' AS per_ing_ultanio_anio,--smallint] NOT NULL,
'' AS per_ind_pep,--char](1) NOT NULL,
'' AS per_motivo_bloqueo,--char](4) NOT NULL,
'' AS per_fec_bloqueo,--datetime] NOT NULL,
'' AS per_fec_desbloqueo,--datetime] NOT NULL,
'' AS per_taxid,--varchar](20) NOT NULL,
'' AS cod_banco,--char](10) NOT NULL,
'' AS per_fec_fallecimiento,--varchar](10) NOT NULL,
'' AS per_no_valida_kyc,--char](1) NOT NULL,
'' AS per_nombre_ejecutivo,--varchar](60) NOT NULL,
'' AS per_nombre_sucursal,--varchar](60) NOT NULL,
'' AS per_tipo_bloqueo,--varchar](50) NOT NULL,
'' AS per_cod_cli_alt,--varchar](50) NOT NULL,
'' AS per_pais_resi,--char](3) NOT NULL,
'' AS per_rela_corr,--char](10) NOT NULL,
'' AS per_parti_soc,--char](1) NOT NULL,
'' AS per_soc_rut,--varchar](20) NOT NULL,
'' AS per_soc_raz_social,--varchar](80) NOT NULL,
'' AS per_soc_giro,--int] NOT NULL,
'' AS per_soc_porc_parti,--numeric](5, 2) NOT NULL,
'' AS per_emp_rut,--varchar](20) NOT NULL,
'' AS per_emp_nom_fantasia,--varchar](80) NOT NULL,
'' AS per_emp_giro,--varchar](2) NOT NULL,
'' AS per_glei,--varchar](50) NOT NULL,
'' AS per_origen_ingreso,--char](3) NOT NULL,
'' AS per_pais_ori_fondos,--varchar](3) NOT NULL,
'' AS per_pro_ori_fondos,--varchar](80) NOT NULL,
'' AS per_rela_corr_art100,--char](1) NOT NULL,
'' AS per_clasif_cli,--varchar](10) NOT NULL,
'' AS par_ent_uaf,--varchar](10) NOT NULL,
'' AS naturaleza_ent_uaf,--varchar](150) NOT NULL,
'' AS folio_ent_uaf,--int] NOT NULL,
'' AS nacionalidad_ent_uaf,--char](2) NOT NULL,
'' AS rut_ent_uaf,--char](11) NOT NULL,
'' AS nombre_ent_uaf,--varchar](200) NOT NULL,
'' AS relacion_ent_uaf --varchar](200) NOT NULL
from "BSCL.SocioNegocio.BD::PER.Persona" as PER
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad" AND  IDENT."IdTipoDocumentoIdentidad" = 'R'
LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" AS PAIS ON PER."IdPaisNacimiento" = PAIS."IdPais"
WHERE IDENT."DocumentoIdentidad"  like '%-_'  ---FIltra basura
;
--GO
GRANT SELECT ON "BSCL.Homologacion.BD.Vistas::HomologaPersona" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
--GOahi conec