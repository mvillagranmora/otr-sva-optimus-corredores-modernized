SET SCHEMA "BSCL";  
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::InformeMensualInversionistasRV";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::InformeMensualInversionistasRV"
( 
    -- Parametros de Entrada
 	IN p_IdSolicitudCircular 		  BIGINT DEFAULT 0
      
)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
  BEGIN
  	  
	declare v_ParametrosSolicitudFini DATE DEFAULT '0001-01-01';
	declare v_ParametrosSolicitudFter DATE DEFAULT '2050-01-01';

	declare v_IdSolicitudCircular int;
    declare v_Periodo            nvarchar(7);
	declare v_Count				 int DEFAULT 0;	
	declare v_CondFinal			 int DEFAULT 0;
	declare v_CondFecha          int DEFAULT 0;
	declare v_IdEmpresa			 BIGINT DEFAULT 0;
	declare v_IdCuentaPropia     BIGINT DEFAULT 0;
	declare v_RazonSocial        NVARCHAR(150);
	declare v_CodigoCorredora    NVARCHAR(2);
	DECLARE v_ParametrosSolicitudRut    TABLE("TipoParametro" NVARCHAR(100),"Valor" NVARCHAR(100));
    
    --------------------
	--DECLARE Manejo de Errores
	-------------------- 
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		RESIGNAL;
	END;

	--------------------
	--LOGICA
	--------------------
	
	v_ParametrosSolicitudRut = select '' as "TipoParametro", '' as "Valor" from dummy where 1=2;
	v_ParametrosSolicitudNemo = select '' as "TipoParametro", '' as "Valor" from dummy where 1=2;
	
	v_ClientesRepactacionesFecha = select 0            as "IdCliente", 
	                                      '0001-01-01' AS "Fecha", 
	                                      0 AS "Monto", 
	                                      '' AS "IdMoneda",
	                                      '' AS "IdConcepto", 
	                                      '' AS "TipoMovimiento"
	                                      from dummy where 1=2;
	
	select top 1 "Valor" into v_IdEmpresa from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'EMP';
	
	select top 1 "Valor" into v_ParametrosSolicitudFini from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'FINI';

	select count(1) into v_CondFecha from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" 
	where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular 
	and "SP"."TipoParametro" = 'FTER';
	
	SELECT TO_CHAR(:v_ParametrosSolicitudFini,'MM/YYYY') INTO v_Periodo FROM dummy;
	
	IF(v_CondFecha >0) THEN
	select top 1 "Valor" into v_ParametrosSolicitudFter from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'FTER';
	END IF;
	
	CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(0,CURRENT_UTCTIMESTAMP,0,'BSCL.Normativa.BD.Servicios.NOR::InformeMensualInversionistasRV','INICIO','INICIO');

	v_palos = select CC."IdCliente",
					 CT."FechaOperacion",
				     CT."FolioTransaccion",
				     CASE CT."AccionTipoOperacion" WHEN 'COMPRA' THEN CT."MontoTransaccion" ELSE 0 END AS "MontoCompra", 
				     CASE CT."AccionTipoOperacion" WHEN 'VENTA' THEN CT."MontoTransaccion" ELSE 0 END AS "MontoVenta",
				     CASE CT."AccionTipoOperacion" WHEN 'COMPRA' THEN 'C' ELSE 'V' END AS "TipoOperacion",
				     CT."MontoTransaccion"
	from "BSCL.Orden.BD.Vistas::ConsultarTransaccion" CT
	inner join "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" CMV ON (CMV."IdTransaccion" = CT."IdTransaccion")
    inner join "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" CC ON (CC."IdComprobante" = CMV."IdComprobante")
    where CMV."IdEstadoFacturacion" = 'FAC'
    and CT."FechaOperacion" between :v_ParametrosSolicitudFini and :v_ParametrosSolicitudFter
    and CT."IdTipoInstrumento" in ('ACN','ETN','CFI','CFM')
    and CT."IdSistemaBursatil" != 'MANUAL';
  
    
	select top 1 SDN."IdPersona" into v_IdCuentaPropia
	from "BSCL.Plataforma.BD::PLA.Empresa" EMP
	inner join "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SDN ON (SDN."IdSocioDeNegocio" = EMP."IdSocioDeNegocio")
	inner join "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC ON (CC."IdCliente" = SDN."IdCliente")
	where EMP."IdEmpresa" = :v_IdEmpresa;

	select top 1 CC."RazonSocial" into v_RazonSocial 
	from "BSCL.Plataforma.BD::PLA.Empresa" EMP
	inner join "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SDN ON (SDN."IdSocioDeNegocio" = EMP."IdSocioDeNegocio")
	inner join "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC ON (CC."IdCliente" = SDN."IdCliente")
	where EMP."IdEmpresa" = :v_IdEmpresa;

	select top 1 CCC."CodigoCorredora" into v_CodigoCorredora 
	from "BSCL.Plataforma.BD::PLA.Empresa" EMP
	inner join "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SDN ON (SDN."IdSocioDeNegocio" = EMP."IdSocioDeNegocio")
	inner join "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraCodigos" CCC ON (CCC."IdPersona" = SDN."IdPersona" AND "IdBolsa" = 'BCS')
	where EMP."IdEmpresa" = :v_IdEmpresa;

	v_countCuentaPropia = SELECT COALESCE(count(REL."IdPersonaOrigen"),0) AS "IdPersonaOrigenCount", "IdPersonaDestino"
						  FROM  "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" REL
						  WHERE REL."IdPersonaOrigen" = :v_IdCuentaPropia and REL."IdEstado" = 'ACT'
						  AND REL."Administracion" = 'RELACIONADOS'
						  GROUP BY "IdPersonaDestino";
						  
	v_count1064Prop = SELECT COALESCE(COUNT("IdCliente"),0) AS "IdClienteCount", "IdCliente" 
   	      			  FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND1064
   	      			  WHERE IND1064."CodigoTipoIndicador" = '1064PROP'
   	      			  AND "ValorIndicador" = 'S'
   	      			  GROUP BY "IdCliente";
   	      			  
    v_countRelCorr = SELECT COALESCE(COUNT("IdCliente"),0) AS "IdClienteCount", "IdCliente"
   	      		     FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND100
   	      			 WHERE IND100."CodigoTipoIndicador" = 'RELCORR'
   	      			 AND "ValorIndicador" = 'S'
   	      			 GROUP BY "IdCliente";
   	      			 
	v_palosBase = select CLI."IdCliente",
					  CLI."IdPersona",
					  CLI."DocumentoIdentidad",
					  PAL."FechaOperacion",
				      PAL."FolioTransaccion",
				      PAL."MontoCompra", 
				      PAL."MontoVenta",
				      PAL."TipoOperacion",
				      PAL."MontoTransaccion",
					  CASE CLI."IdTipoCliente" WHEN 'INSTITUCIONAL' THEN 'I'
							 WHEN 'RETAIL' THEN 'R'
							 ELSE 'O'
				      END AS "Clasificacion", 
				      PAIS."CodigoPais",
				      CASE WHEN COALESCE(CP."IdPersonaOrigenCount",0) > 0 OR 
				      			COALESCE(C1064."IdClienteCount",0) > 0 OR 
				      			COALESCE(CRel."IdClienteCount",0) > 0 THEN 'S' 
				   	  	   ELSE '' 
				   	  END AS "Relacionado"
    from :v_palos PAL
    left join "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON (CLI."IdCliente" = PAL."IdCliente")
	left join "BSCL.Plataforma.BD::REG.Pais" PAIS on (PAIS."IdPais" = CLI."IdPaisResidencia")
	left join :v_countCuentaPropia CP on (CP."IdPersonaDestino" = CLI."IdPersona")
	left join :v_count1064Prop C1064 on (C1064."IdCliente" = PAL."IdCliente")
	left join :v_countRelCorr CRel on (CRel."IdCliente" = PAL."IdCliente");
	
	v_palosFiltroRelacion = select PAL."IdCliente",
					  PAL."IdPersona",
					  PAL."DocumentoIdentidad",
					  PAL."FechaOperacion",
				      PAL."FolioTransaccion",
				      PAL."MontoCompra", 
				      PAL."MontoVenta",
				      PAL."TipoOperacion",
				      PAL."MontoTransaccion",
					  CASE WHEN PAL."Relacionado" = 'S' THEN 'P'
					       WHEN PAL."IdPersona" = :v_IdCuentaPropia THEN 'P'
					       ELSE PAL."Clasificacion"
					  END AS "Clasificacion",
				      PAL."CodigoPais",
				      PAL."Relacionado"
			  from :v_palosBase PAL;
		
	v_palosFiltroNoMila = select PAL."IdCliente",
					  PAL."IdPersona",
					  PAL."DocumentoIdentidad",
					  PAL."FechaOperacion",
				      PAL."FolioTransaccion",
				      PAL."MontoCompra", 
				      PAL."MontoVenta",
				      PAL."TipoOperacion",
				      PAL."MontoTransaccion",
					  CASE WHEN PAL."CodigoPais" NOT IN ('MX','CO','PE','CL') THEN 'N'
					       ELSE PAL."Clasificacion"
					  END AS "Clasificacion",
				      PAL."CodigoPais",
				      PAL."Relacionado"
			  from :v_palosFiltroRelacion PAL;
			  
	v_palosFinal = select PAL."IdCliente",
					  PAL."IdPersona",
					  PAL."DocumentoIdentidad",
					  PAL."FechaOperacion",
				      PAL."FolioTransaccion",
				      PAL."MontoCompra", 
				      PAL."MontoVenta",
				      PAL."TipoOperacion",
				      PAL."MontoTransaccion",
					  CASE WHEN PAL."CodigoPais" IN ('MX','CO','PE') THEN 'M'
					       ELSE PAL."Clasificacion"
					  END AS "Clasificacion",
				      PAL."CodigoPais",
				      PAL."Relacionado"
			  from :v_palosFiltroNoMila PAL;
			  	  		  
		SELECT
							    :v_Periodo AS "Periodo"         		, 
								:v_RazonSocial AS "NombreCorredor"		, 
								:v_CodigoCorredora AS "CodigoCorredor"		, 
								(select COALESCE((sum(pal."MontoTransaccion") / 1000),0) from :v_palosFinal pal where pal."Clasificacion" = 'N') AS "TotalMontoNoMila",
								(select COALESCE((sum(pal."MontoTransaccion") / 1000),0) from :v_palosFinal pal where pal."Clasificacion" = 'N' AND pal."TipoOperacion" = 'C') AS "CompraMontoNoMila"	,
								(select COALESCE((sum(pal."MontoTransaccion") / 1000),0) from :v_palosFinal pal where pal."Clasificacion" = 'N' AND pal."TipoOperacion" = 'V') AS "VentaMontoNoMila"		,
								(select COALESCE(count(distinct(pal."FolioTransaccion")),0) from :v_palosFinal pal where pal."Clasificacion" = 'N') AS "TotalNegNoMila"		,
								(select COALESCE(count(distinct(pal."FolioTransaccion")),0) from :v_palosFinal pal where pal."Clasificacion" = 'N' AND pal."TipoOperacion" = 'C') AS "CompraNegNoMila"		,
								(select COALESCE(count(distinct(pal."FolioTransaccion")),0) from :v_palosFinal pal where pal."Clasificacion" = 'N' AND pal."TipoOperacion" = 'V') AS "VentaNegNoMila"		,
								(select COALESCE(count(distinct(pal."IdCliente")),0) from :v_palosFinal pal where pal."Clasificacion" = 'N') AS "NumeroClientesNoMila"	,
								(select COALESCE((sum(pal."MontoTransaccion") / 1000),0) from :v_palosFinal pal where pal."Clasificacion" = 'M') AS "TotalMontoMila"		,
								(select COALESCE((sum(pal."MontoTransaccion") / 1000),0) from :v_palosFinal pal where pal."Clasificacion" = 'M' AND pal."TipoOperacion" = 'C') AS "CompraMontoMila"		,
								(select COALESCE((sum(pal."MontoTransaccion") / 1000),0) from :v_palosFinal pal where pal."Clasificacion" = 'M' AND pal."TipoOperacion" = 'V') AS "VentaMontoMila"		,
								(select COALESCE(count(distinct(pal."FolioTransaccion")),0) from :v_palosFinal pal where pal."Clasificacion" = 'M') AS "TotalNegMila"			,
								(select COALESCE(count(distinct(pal."FolioTransaccion")),0) from :v_palosFinal pal where pal."Clasificacion" = 'M' AND pal."TipoOperacion" = 'C') AS "CompraNegMila"		,
								(select COALESCE(count(distinct(pal."FolioTransaccion")),0) from :v_palosFinal pal where pal."Clasificacion" = 'M' AND pal."TipoOperacion" = 'V') AS "VentaNegMila"			,
								(select COALESCE(count(distinct(pal."IdCliente")),0) from :v_palosFinal pal where pal."Clasificacion" = 'M') AS "NumeroClientesMila"	,
								(select COALESCE((sum(pal."MontoTransaccion") / 1000),0) from :v_palosFinal pal where pal."Clasificacion" = 'I') AS "TotalMontoLocal"		,
								(select COALESCE((sum(pal."MontoTransaccion") / 1000),0) from :v_palosFinal pal where pal."Clasificacion" = 'I' AND pal."TipoOperacion" = 'C') AS "CompraMontoLocal"		,
								(select COALESCE((sum(pal."MontoTransaccion") / 1000),0) from :v_palosFinal pal where pal."Clasificacion" = 'I' AND pal."TipoOperacion" = 'V') AS "VentaMontoLocal"		,
								(select COALESCE(count(distinct(pal."FolioTransaccion")),0) from :v_palosFinal pal where pal."Clasificacion" = 'I') AS "TotalNegLocal"		,
								(select COALESCE(count(distinct(pal."FolioTransaccion")),0) from :v_palosFinal pal where pal."Clasificacion" = 'I' AND pal."TipoOperacion" = 'C') AS "CompraNegLocal"		,
								(select COALESCE(count(distinct(pal."FolioTransaccion")),0) from :v_palosFinal pal where pal."Clasificacion" = 'I' AND pal."TipoOperacion" = 'V') AS "VentaNegLocal"		,
								(select COALESCE(count(distinct(pal."IdCliente")),0) from :v_palosFinal pal where pal."Clasificacion" = 'I') AS "NumeroClientesLocal"	,
								(select COALESCE((sum(pal."MontoTransaccion") / 1000),0) from :v_palosFinal pal where pal."Clasificacion" = 'R') AS "TotalMontoRetail"		,
								(select COALESCE((sum(pal."MontoTransaccion") / 1000),0) from :v_palosFinal pal where pal."Clasificacion" = 'R' AND pal."TipoOperacion" = 'C') AS "CompraMontoRetail"	,
								(select COALESCE((sum(pal."MontoTransaccion") / 1000),0) from :v_palosFinal pal where pal."Clasificacion" = 'R' AND pal."TipoOperacion" = 'V') AS "VentaMontoRetail"		,
								(select COALESCE(count(distinct(pal."FolioTransaccion")),0) from :v_palosFinal pal where pal."Clasificacion" = 'R') AS "TotalNegRetail"		,
								(select COALESCE(count(distinct(pal."FolioTransaccion")),0) from :v_palosFinal pal where pal."Clasificacion" = 'R' AND pal."TipoOperacion" = 'C') AS "CompraNegRetail"		,
								(select COALESCE(count(distinct(pal."FolioTransaccion")),0) from :v_palosFinal pal where pal."Clasificacion" = 'R' AND pal."TipoOperacion" = 'V') AS "VentaNegRetail"		,
								(select COALESCE(count(distinct(pal."IdCliente")),0) from :v_palosFinal pal where pal."Clasificacion" = 'R') AS "NumeroClientesRetail"	,
								(select COALESCE((sum(pal."MontoTransaccion") / 1000),0) from :v_palosFinal pal where pal."Clasificacion" = 'P') AS "TotalMontoPropia"		,
								(select COALESCE((sum(pal."MontoTransaccion") / 1000),0) from :v_palosFinal pal where pal."Clasificacion" = 'P' AND pal."TipoOperacion" = 'C') AS "CompraMontoPropia"	,
								(select COALESCE((sum(pal."MontoTransaccion") / 1000),0) from :v_palosFinal pal where pal."Clasificacion" = 'P' AND pal."TipoOperacion" = 'V') AS "VentaMontoPropia"		,
								(select COALESCE(count(distinct(pal."FolioTransaccion")),0) from :v_palosFinal pal where pal."Clasificacion" = 'P') AS "TotalNegPropia"		,
								(select COALESCE(count(distinct(pal."FolioTransaccion")),0) from :v_palosFinal pal where pal."Clasificacion" = 'P' AND pal."TipoOperacion" = 'C') AS "CompraNegPropia"		,
								(select COALESCE(count(distinct(pal."FolioTransaccion")),0) from :v_palosFinal pal where pal."Clasificacion" = 'P' AND pal."TipoOperacion" = 'V') AS "VentaNegPropia"		,
								(select COALESCE(count(distinct(pal."IdCliente")),0) from :v_palosFinal pal where pal."Clasificacion" = 'P') AS "NumeroClientesPropia"	,
								(select COALESCE((sum(pal."MontoTransaccion") / 1000),0) from :v_palosFinal pal where pal."Clasificacion" = 'O') AS "TotalMontoOtros"		,
								(select COALESCE((sum(pal."MontoTransaccion") / 1000),0) from :v_palosFinal pal where pal."Clasificacion" = 'O' AND pal."TipoOperacion" = 'C') AS "CompraMontoOtros"		,
								(select COALESCE((sum(pal."MontoTransaccion") / 1000),0) from :v_palosFinal pal where pal."Clasificacion" = 'O' AND pal."TipoOperacion" = 'V') AS "VentaMontoOtros"		,
								(select COALESCE(count(distinct(pal."FolioTransaccion")),0) from :v_palosFinal pal where pal."Clasificacion" = 'O') AS "TotalNegOtros"		,
								(select COALESCE(count(distinct(pal."FolioTransaccion")),0) from :v_palosFinal pal where pal."Clasificacion" = 'O' AND pal."TipoOperacion" = 'C') AS "CompraNegOtros"		,
								(select COALESCE(count(distinct(pal."FolioTransaccion")),0) from :v_palosFinal pal where pal."Clasificacion" = 'O' AND pal."TipoOperacion" = 'V') AS "VentaNegOtros"		,
								(select COALESCE(count(distinct(pal."IdCliente")),0) from :v_palosFinal pal where pal."Clasificacion" = 'O') AS "NumeroClientesOtros"	
								FROM dummy;		
	
	CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(0,CURRENT_UTCTIMESTAMP,0,'BSCL.Normativa.BD.Servicios.NOR::InformeMensualInversionistasRV','FIN','FIN');

END;
GO