SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.CuentaInversion.CrystalReports.Servicios::ComprobanteCuentaInversion";

CREATE PROCEDURE
-- 
--  Proposito: Recuperar información asociado al cmprobante de cuenta inversion
--  Autor: NRM - BCS
--  Fecha Creacion: 16/01/2016
--
"BSCL.CuentaInversion.CrystalReports.Servicios::ComprobanteCuentaInversion" (
     IN p_IdEmpresa 				BIGINT,
     IN p_IdCargoAbonoMovimiento	BIGINT
 )
 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	DECLARE v_FechaHoraSistema 	NVARCHAR(40);
	DECLARE v_SegundosZonaHoraria BIGINT;
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(:p_IdEmpresa,v_FechaHoraSistema);
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion"(:p_IdEmpresa, 'PLA.SegundosZonaHoraria', v_SegundosZonaHoraria);
	
	v_resultado = SELECT DISTINCT
			CAM."IdCargoAbonoMovimiento"
		   ,TMOV."Nombre" AS "NombreTipoMovimiento"
		   ,CPT."Nombre" AS "NombreConcepto"		   
		   ,CLI."NombreCompleto" as "NombreCliente"
		   ,POR."CodigoPortafolio" ||' - '|| POR."Proposito" as "Portafolio"		   
		   ,EPOR."NombreCompletoEjecutivo" as "NombreEjecutivo"
		   ,EPOR."NombreSucursal"		   
		   ,CAM."FechaSistema" as "FechaMovimiento"
		   ,UING."NombreCompleto" as "IngresadoPor"
		   ,COALESCE(CAM."FechaCertificacion",'0001-01-01') AS "FechaCertificacion"
		   ,UCER."NombreCompleto" as "CertificadoPor"		   
		   ,CAM."FechaEfectivaMovimiento" as "FechaEfectiva"
		   ,CI."IdMoneda"
		   ,CAM."Monto"
		   ,CASE WHEN COALESCE(CAM."IdTipoComprobante",'') = '' THEN '' ELSE CAST(CAM."IdTipoComprobante" as NVARCHAR) END as "IdTipoComprobante"
		   ,CASE WHEN COALESCE(CAM."FechaComprobante",'0001-01-01') = '0001-01-01' THEN '' ELSE CAST(CAM."FechaComprobante" as NVARCHAR) END AS "FechaComprobante"
		   ,CASE WHEN COALESCE(CAM."IdComprobante",0) = 0 THEN '' ELSE CAST(CAM."IdComprobante" as NVARCHAR) END as "SecuenciaComprobante"
		   ,CASE WHEN COALESCE(CAM."FolioComprobante",'') = '' THEN '' ELSE CAST(CAM."FolioComprobante" as NVARCHAR) END as "FolioComprobante"
		   ,EST."Nombre" as "NombreEstado"
		   ,CAM."Descripcion"   
		   ,CASE WHEN COALESCE(TRA."IdCargoAbonoMovimiento",0)=0 THEN '-' ELSE CAST(TRA."IdCargoAbonoMovimiento" as NVARCHAR) END as "IdCargoAbonoMovimientoTransferencia"
		   ,TRA."NombreTipoMovimiento" as "NombreTipoMovimientoTransferencia"
		   ,TRA."NombreCliente" as "NombreClienteTransferencia"
		   ,TRA."Portafolio" as "PortafolioTransferencia"
		   ,TRA."NombreEjecutivo" as "NombreEjecutivoTransferencia"
		   ,TRA."NombreSucursal" as "NombreSucursalTransferencia"
		   ,CAM."EsCargoAbonoPorTraspaso"
		   
		   ,DGEN."RazonSocial" AS "RazonCorredora"
		   ,DGEN."RUT" AS "RutCorredora"
		   ,DGEN."Direccion"   AS "DireccionCorredora"		   
		   ,DGEN."Telefono" AS "TelefonoCorredora"		   
		   ,SUBSTRING (:v_FechaHoraSistema,1,10) AS "FechaSistema"
		   ,SUBSTRING (ADD_SECONDS(:v_FechaHoraSistema,:v_SegundosZonaHoraria),12,8) AS "HoraSistema"
	  FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS CAM
	 INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CI ON CI."IdCuentaInversion" = CAM."IdCuentaInversion"
	 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON CLI."IdCliente" = CI."IdCliente"
  	 INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as POR ON POR."IdPortafolio" = CI."IdPortafolio"
  	 INNER JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto" as UING ON UING."IdUsuario" = CAM."IdUsuario"
  	 INNER JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" as DGEN ON DGEN."IdEmpresa" = CAM."IdEmpresa"
  	  LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto" as UCER ON UCER."IdUsuario" = CAM."IdUsuarioCertificador"   
      LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.TipoMovimiento" AS TMOV ON  TMOV."IdTipoMovimiento" = CAM."IdTipoMovimiento"      
   	  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST ON (EST."IdEstado" = CAM."IdEstado")
	  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS CPT ON  CPT."IdConcepto" = CAM."IdConcepto"
	  LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio" AS EPOR ON EPOR."IdPortafolio" = CI."IdPortafolio" AND EPOR."EsEjecutivoVigente" = 'S'
	  LEFT OUTER JOIN (
	  					SELECT DISTINCT
								CAMTRA."IdCargoAbonoMovimiento"
							   ,TMOV."Nombre" AS "NombreTipoMovimiento"		   
							   ,CLI."NombreCompleto" as "NombreCliente"
							   ,POR."CodigoPortafolio" ||' - '|| POR."Proposito" as "Portafolio"		   
							   ,EPOR."NombreCompletoEjecutivo" as "NombreEjecutivo"
							   ,EPOR."NombreSucursal"
						  FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS CAMTRA
						 INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CI ON CI."IdCuentaInversion" = CAMTRA."IdCuentaInversion"
						 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON CLI."IdCliente" = CI."IdCliente"
					  	 INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as POR ON POR."IdPortafolio" = CI."IdPortafolio"
					  	  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.TipoMovimiento" AS TMOV ON  TMOV."IdTipoMovimiento" = CAMTRA."IdTipoMovimiento"
						  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS CPT ON  CPT."IdConcepto" = CAMTRA."IdConcepto"
						  LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio" AS EPOR ON EPOR."IdPortafolio" = CI."IdPortafolio" AND EPOR."EsEjecutivoVigente" = 'S'
						 WHERE CAMTRA."IdCargoAbonoRelacionadoTraspaso" > 0
	  				  ) as TRA
	  			   ON CAM."IdCargoAbonoRelacionadoTraspaso" = TRA."IdCargoAbonoMovimiento"		     
	 WHERE CAM."IdCargoAbonoMovimiento" = :p_IdCargoAbonoMovimiento
	   AND CAM."IdEmpresa" = :p_IdEmpresa;
	   
	v_logo = select TOP 1
        "Binario" AS "LogoCorredora"
        from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
        WHERE "NombreImagen" = 'LOGO_CORREDOR' AND "IdEmpresa" = p_IdEmpresa ORDER BY "Version" DESC;
        
    SELECT *
    FROM :v_resultado
    CROSS JOIN :v_logo;
    
END;

  