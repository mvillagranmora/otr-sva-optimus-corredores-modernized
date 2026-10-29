SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::TRA.ConsultarTraspasosAPV"; 
 
CREATE VIEW "BSCL.APV.BD.Vistas::TRA.ConsultarTraspasosAPV"  AS 

SELECT Traspasos."IngresoSolicitud"
     , Traspasos."CierreSolicitud",
       Traspasos."IdEmpresa"
     , Traspasos."IdTraspaso"
	 , Traspasos."IdContratoSAAPV"
	 , MAX(Traspasos."FolioContratoSAAPV")			AS "FolioContratoSAAPV"
	 , Traspasos."FechaIngreso"
	 , Traspasos."FechaEgreso"
	 , Traspasos."FechaModificacion"
	 , Traspasos."TipoMovimiento"
	 , Traspasos."IdPlan"
	 , Traspasos."NombrePlan"
	 , Traspasos."CodigoRazonRetiro"
	 , Traspasos."RazonRetiro"
	 , CASE WHEN Traspasos."IdEstado" <> 'COM' THEN (CASE WHEN Traspasos."Dias" < 0 THEN 0 ELSE Traspasos."Dias" END) ELSE 0 END AS "Dias"  
	 , Traspasos."CodigoTipoTraspaso"
	 , Traspasos."IdCliente"
	 , Traspasos."IdCategoriaInversionista"
	 , Traspasos."Trabajador"
	 , Traspasos."DocumentoIdentidadDefecto"
	 , Traspasos."IdSocioNegocioCli"
	 , Traspasos."IdPersonaCli"
	 , Traspasos."NombreCompleto"
	 , Traspasos."IdPersona"
	 , Traspasos."IdSocioNegocioEMP"
	 , Traspasos."RazonSocial"
	 , Traspasos."IdEmpleador"
	 , Traspasos."CodigoEntidad"
	 , Traspasos."CodigoTipoAhorro"	
	 , Traspasos."NombreEntidad"
	 , Traspasos."DocumentoEntidad"
	 , Traspasos."Descripcion"
	 , Traspasos."IdEstado"
	 , Traspasos."IdEstadoContrato"
	 , COALESCE(Estados."Nombre", COALESCE(Estados."Codigo",'')) AS "EstadoNombre"
	 , MAX(Traspasos."IdPerfilComercialBaseOrigen") 	AS "IdPerfilComercialBaseOrigen"
	 , SUM(Traspasos."MontoTotalTraspaso")				AS "MontoTotalTraspaso"
	 , SUM(Traspasos."UFTotalTraspaso")					AS "UFTotalTraspaso"
	 , CASE WHEN Traspasos."CodigoTipoTraspaso" = 'TOTAL' AND Traspasos."TipoMovimiento"='EGRESO' THEN 100		
		  WHEN Traspasos."CodigoTipoTraspaso" = 'PARCIAL' AND Traspasos."TipoMovimiento"='EGRESO' AND SUM(Traspasos."PorcentajeTotalTraspaso") > 100  THEN 100
		  ELSE SUM(Traspasos."PorcentajeTotalTraspaso")		END	AS "PorcentajeTotalTraspaso"
	 , SUM(Traspasos."MontoPesoDetalle")				AS "MontoPesoDetalle"
	 , SUM(Traspasos."MontoUFDetalle")					AS "MontoUFDetalle"
	 , SUM(Traspasos."PorcentajeDetalle")				AS "PorcentajeDetalle"
	 , COALESCE(OrdenTraspaso."MontoOrdenTraspaso",0)	AS "MontoOrdenTraspaso"
	 , max(Traspasos."IdMatrizAgrupador") AS "IdMatrizAgrupador"
	 , Traspasos."EsADC"
	 , Traspasos."Canal"
	 , Traspasos."DescripcionCanal"
	 , CASE WHEN SUM(Traspasos."MontoTotalTraspaso") > 0 
	 	THEN ((SUM(Traspasos."MontoPesoDetalle") - SUM(Traspasos."MontoTotalTraspaso"))/SUM(Traspasos."MontoTotalTraspaso") )*100
	 	ELSE 0 END AS "DiferenciaPorcentajeMonto"
FROM (
		SELECT DISTINCT CASE WHEN Traspaso."TipoMovimiento" = 'INGRESO' THEN Traspaso."FechaIngreso" ELSE Traspaso."FechaEgreso" END 								AS "IngresoSolicitud"
			 , Traspaso."CierreSolicitud"							AS "CierreSolicitud"
             , Traspaso."IdEmpresa"
		     , Traspaso."IdTraspaso"
			 , Traspaso."IdContratoSAAPV"
			 , Contrato."FolioContratoSAAPV"					
			 , Traspaso."FechaIngreso"
			 , Traspaso."FechaEgreso"
			 , Traspaso."FechaModificacion"
			 , Traspaso."TipoMovimiento"
			 , Contrato."IdPlan"
			 , Contrato."IdEstado" 									AS "IdEstadoContrato"
			 , Contrato."NombrePlan"
			 , RazonRetiro."Codigo" 								AS "CodigoRazonRetiro"
			 , RazonRetiro."Nombre" 								AS "RazonRetiro"
			 , DAYS_BETWEEN (
					CURRENT_DATE,
					ADD_DAYS (TO_DATE (Traspaso."FechaIngreso", 'YYYY-MM-DD'), ConfiguracionAPV."Valor") 
					) AS "Dias"  --****  (FECHA ACTUAL) - (FECHA DE INGRESO + DiAS VIGENCIA) ***
			 , CASE WHEN Traspaso."CodigoTipoTraspaso" = 'TOT' THEN 'TOTAL'
			 		WHEN Traspaso."CodigoTipoTraspaso" = 'PAR' THEN 'PARCIAL'
			   END AS "CodigoTipoTraspaso"
			 , Traspaso."IdCliente"
			 , COALESCE(Cliente."IdCategoriaInversionista",Contrato."IdCategoriaInversionista") AS "IdCategoriaInversionista"
			 , COALESCE(Portafolio."IdPerfilComercialBaseOrigen",Contrato."IdPerfilComercial") 			AS "IdPerfilComercialBaseOrigen" 
			 , Contrato."Trabajador"
			 , PersonaCli."DocumentoIdentidad"						AS "DocumentoIdentidadDefecto"
			 , SocioNegocioCli."IdSocioDeNegocio" 					AS "IdSocioNegocioCli"
			 , SocioNegocioCli."IdPersona" 							AS "IdPersonaCli"
			 , PersonaCli."NombreCompleto"
			 , Traspaso."IdPersona"
			 , COALESCE(SocioNegocioEmp."IdSocioDeNegocio",0) 		AS "IdSocioNegocioEMP"
			 , COALESCE(PersonaEmp."RazonSocial", '') 				AS "RazonSocial"
			 , Contrato."IdEmpleador"								AS "IdEmpleador"
			 , EntidadRecaudadora."Codigo"							AS "CodigoEntidad"
			 , Traspaso."CodigoTipoAhorro"	
			 , Traspaso."NombreEntidad"
			 , Traspaso."DocumentoEntidad"
			 , Traspaso."Descripcion"
		 	 , 0 	AS "MontoTotalTraspaso"
			 , 0 	AS "UFTotalTraspaso"
			 , 0 	AS "PorcentajeTotalTraspaso"
			 , COALESCE(DetalleTraspaso."MontoPeso",0)				AS "MontoPesoDetalle"
			 , COALESCE(DetalleTraspaso."MontoUF",0)				AS "MontoUFDetalle"
			 , COALESCE(DetalleTraspaso."Porcentaje",0)				AS "PorcentajeDetalle"
			 , Traspaso."IdEstado"
			 ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
			 ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE("MAT"."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
			 ,Traspaso."Canal"
			 ,COALESCE(Canal."Nombre",'') AS "DescripcionCanal"
		  FROM "BSCL.APV.BD::TRA.Traspaso" AS Traspaso
		INNER JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" AS Contrato
		 	    ON Contrato."IdContratoSAAPV" = Traspaso."IdContratoSAAPV"
		 	   AND Contrato."IdCliente" = Traspaso."IdCliente"
		 	   AND Contrato."CodigoTipoSolicitud" = 'TRA'
		INNER JOIN "BSCL.APV.BD::TRA.DetalleTraspaso" AS DetalleTraspaso
				ON DetalleTraspaso."IdTraspaso" = Traspaso."IdTraspaso"
		INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cliente
		  	    ON Cliente."IdCliente" = Traspaso."IdCliente"
		INNER JOIN "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" AS PortafolioContrato
			    ON PortafolioContrato."IdContratoSAAPV" = Contrato."IdContratoSAAPV"
			   AND PortafolioContrato."IdPortafolio" = DetalleTraspaso."IdPortafolio"
		INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
				ON Portafolio."IdCliente" = Cliente."IdCliente"
			   AND Portafolio."IdPortafolio" = PortafolioContrato."IdPortafolio"
			   --AND Portafolio."IdMatrizAgrupadorAPV" != 0
		LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS SocioNegocioCli
					 ON SocioNegocioCli."IdCliente" = Traspaso."IdCliente"
					AND SocioNegocioCli."IdEmpresa" = Traspaso."IdEmpresa"
		LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona"	AS PersonaCli
					 ON SocioNegocioCli."IdPersona" = PersonaCli."IdPersona"
					--AND SocioNegocioCli."CodigoTipoPersona" = 'N'
		LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS SocioNegocioEmp
					 ON	SocioNegocioEmp."IdPersona" = Traspaso."IdPersona"
					AND SocioNegocioEmp."IdEmpresa" = Traspaso."IdEmpresa"
		LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.Persona"	AS PersonaEmp
					 ON SocioNegocioEmp."IdPersona" = PersonaEmp."IdPersona"
					--AND SocioNegocioCli."CodigoTipoPersona" = 'J'
		LEFT OUTER JOIN "BSCL.APV.BD::TRA.RazonRetiro" AS RazonRetiro
					 ON RazonRetiro."Codigo" = Traspaso."RazonRetiro"		
		LEFT OUTER JOIN "BSCL.APV.BD::PLA.ConfiguracionAPV" ConfiguracionAPV
					 ON ConfiguracionAPV."IdEmpresa" = Traspaso."IdEmpresa" 
				    AND "CodigoParametro" = 'APV.DiasVigencia'
		LEFT OUTER JOIN "BSCL.APV.BD::SCS.EntidadRecaudadora" AS EntidadRecaudadora
					 ON EntidadRecaudadora."IdPersona" = SocioNegocioEmp."IdPersona"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" MAT ON MAT."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND Portafolio."IdMatrizAgrupadorAPV" = 0
		LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" Canal ON Canal."IdTipoCanalIngreso" = Traspaso."Canal"
		UNION ALL
		SELECT  CASE WHEN Traspaso."TipoMovimiento" = 'INGRESO' THEN Traspaso."FechaIngreso" ELSE Traspaso."FechaEgreso" END 								AS "IngresoSolicitud"
             , Traspaso."CierreSolicitud"							AS "CierreSolicitud"
             , Traspaso."IdEmpresa"
		     , Traspaso."IdTraspaso"
			 , Traspaso."IdContratoSAAPV"
			 , Contrato."FolioContratoSAAPV"
			 , Traspaso."FechaIngreso"
			 , Traspaso."FechaEgreso"
			 , Traspaso."FechaModificacion"
			 , Traspaso."TipoMovimiento"
			 , Contrato."IdPlan"
			 , Contrato."IdEstado" 									AS "IdEstadoContrato"
			 , Contrato."NombrePlan"
			 , RazonRetiro."Codigo" 								AS "CodigoRazonRetiro"
			 , RazonRetiro."Nombre" 								AS "RazonRetiro"
			 , DAYS_BETWEEN (
					CURRENT_DATE,
					ADD_DAYS (TO_DATE (Traspaso."FechaIngreso", 'YYYY-MM-DD'), ConfiguracionAPV. "Valor") 
					) AS "Dias"  --****  (FECHA ACTUAL) - (FECHA DE INGRESO + DiAS VIGENCIA) *** 
			 , CASE WHEN Traspaso."CodigoTipoTraspaso" = 'TOT' THEN 'TOTAL'
			 		WHEN Traspaso."CodigoTipoTraspaso" = 'PAR' THEN 'PARCIAL'
			   END AS "CodigoTipoTraspaso"
			 , Traspaso."IdCliente"
			 , COALESCE(Cliente."IdCategoriaInversionista",Contrato."IdCategoriaInversionista") AS "IdCategoriaInversionista"
			 , COALESCE(Portafolio."IdPerfilComercialBaseOrigen",Contrato."IdPerfilComercial") 			AS "IdPerfilComercialBaseOrigen"  
			 , Contrato."Trabajador"
			 , PersonaCli."DocumentoIdentidad"						AS "DocumentoIdentidadDefecto"
			 , SocioNegocioCli."IdSocioDeNegocio" 					AS "IdSocioNegocioCli"
			 , SocioNegocioCli."IdPersona" 							AS "IdPersonaCli"
			 , PersonaCli."NombreCompleto"
			 , Traspaso."IdPersona"
			 , COALESCE(SocioNegocioEmp."IdSocioDeNegocio",0) 		AS "IdSocioNegocioEMP"
			 , COALESCE(PersonaEmp."RazonSocial", '') 					AS "RazonSocial"
			 , Contrato."IdEmpleador"								AS "IdEmpleador"
			 , EntidadRecaudadora."Codigo"							AS "CodigoEntidad"
			 , Traspaso."CodigoTipoAhorro"	
			 , Traspaso."NombreEntidad"
			 , Traspaso."DocumentoEntidad"
			 , Traspaso."Descripcion"
		 	 , COALESCE(DetalleOrigenTraspaso."MontoTraspaso",0) 		AS "MontoTotalTraspaso"
			 , COALESCE(DetalleOrigenTraspaso."UFTraspaso",0) 			AS "UFTotalTraspaso"
			 , COALESCE(DetalleOrigenTraspaso."PorcentajeTraspaso",0) 	AS "PorcentajeTotalTraspaso"
			 , 0				AS "MontoPesoDetalle"
			 , 0				AS "MontoUFDetalle"
			 , 0			AS "PorcentajeDetalle"
			 , Traspaso."IdEstado"
			 ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
			 ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE("MAT"."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
			 ,Traspaso."Canal"
			 ,COALESCE(Canal."Nombre",'') AS "DescripcionCanal"
		  FROM "BSCL.APV.BD::TRA.Traspaso" AS Traspaso
		LEFT OUTER JOIN "BSCL.APV.BD::TRA.DetalleOrigenTraspaso" AS DetalleOrigenTraspaso
					 ON DetalleOrigenTraspaso."IdTraspaso" = Traspaso."IdTraspaso"
		LEFT OUTER JOIN "BSCL.APV.BD::TRA.RazonRetiro" AS RazonRetiro
					 ON RazonRetiro."Codigo" = Traspaso."RazonRetiro"
		LEFT OUTER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cliente
					 ON Cliente."IdCliente" = Traspaso."IdCliente"
		LEFT OUTER JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" AS Contrato
		 		 	 ON Contrato."IdContratoSAAPV" = Traspaso."IdContratoSAAPV"
		 		 	AND Contrato."IdCliente" = Traspaso."IdCliente"
		 			AND Contrato."CodigoTipoSolicitud" = 'TRA'
		LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
				     ON Portafolio."IdCliente" = Cliente."IdCliente"
				    AND Portafolio."IdPortafolio" = Contrato."IdPortafolio"
			        --AND Portafolio."IdMatrizAgrupadorAPV" != 0
		LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS SocioNegocioCli
					 ON SocioNegocioCli."IdCliente" = Traspaso."IdCliente"
					AND SocioNegocioCli."IdEmpresa" = Traspaso."IdEmpresa"
		LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona"	AS PersonaCli
					 ON SocioNegocioCli."IdPersona" = PersonaCli."IdPersona"
					--AND SocioNegocioCli."CodigoTipoPersona" = 'N'
		LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS SocioNegocioEmp
					 ON	SocioNegocioEmp."IdPersona" = Traspaso."IdPersona"
					AND SocioNegocioEmp."IdEmpresa" = Traspaso."IdEmpresa"
		LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.Persona"	AS PersonaEmp
					 ON SocioNegocioEmp."IdPersona" = PersonaEmp."IdPersona"
					--AND SocioNegocioCli."CodigoTipoPersona" = 'J'
		LEFT OUTER JOIN "BSCL.APV.BD::PLA.ConfiguracionAPV" ConfiguracionAPV
					 ON ConfiguracionAPV."IdEmpresa" = Traspaso."IdEmpresa" AND  "CodigoParametro" = 'APV.DiasVigencia'
		LEFT OUTER JOIN "BSCL.APV.BD::SCS.EntidadRecaudadora" AS EntidadRecaudadora
					 ON EntidadRecaudadora."IdPersona" = SocioNegocioEmp."IdPersona"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" MAT ON MAT."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND Portafolio."IdMatrizAgrupadorAPV" = 0
		LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" Canal ON Canal."IdTipoCanalIngreso" = Traspaso."Canal"
		) AS Traspasos
LEFT OUTER JOIN (	SELECT OrdenesTraspaso."IdTraspaso"
						, SUM(OrdenesTraspaso."MontoOrdenTraspaso") AS "MontoOrdenTraspaso"
					FROM	(	SELECT 	 OrdenTraspaso."IdTraspaso"
						  				,SUM(OrdenFondosMutuos."Monto") AS "MontoOrdenTraspaso"
								FROM "BSCL.APV.BD::TRA.OrdenTraspaso" AS OrdenTraspaso
								INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS OrdenFondosMutuos
									    ON OrdenFondosMutuos."IdSolicitudAporteRescate" = OrdenTraspaso."IdSolicitudAporteRescate"
								WHERE OrdenFondosMutuos."Estado" IN ('EJE','CRT')
								GROUP BY "IdTraspaso"
								UNION ALL
					 			SELECT 	 OrdenTraspaso."IdTraspaso"
						  				,SUM(Orden."MontoLiquidacion") AS "MontoOrdenTraspaso"
								FROM "BSCL.APV.BD::TRA.OrdenTraspaso" AS OrdenTraspaso
								INNER JOIN "BSCL.Orden.BD::ORD.Orden" AS Orden
										ON Orden."IdOrden" = OrdenTraspaso."IdSolicitudAporteRescate"
								INNER JOIN "BSCL.Orden.BD::ORD.Asignacion" AS ASG
								ON OrdenTraspaso."IdSolicitudAporteRescate" = ASG."IdOrden"
								INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MC
								ON ASG."IdAsignacion" = "MC"."IdAsignacion"
								AND MC."IdEstadoFacturacion" = 'FAC'
								GROUP BY "IdTraspaso" ) AS OrdenesTraspaso
					GROUP BY OrdenesTraspaso."IdTraspaso") AS OrdenTraspaso
ON OrdenTraspaso."IdTraspaso" = Traspasos."IdTraspaso"
LEFT JOIN "BSCL.APV.BD::APV.Estado" Estados ON Traspasos."IdEstado" = Estados."Codigo" AND Estados."NombreEntidad" = 'CONTRATO'
GROUP BY  Traspasos."IngresoSolicitud",Traspasos."CierreSolicitud",
	   Traspasos."IdEmpresa"
     , Traspasos."IdTraspaso"
	 , Traspasos."IdContratoSAAPV"
	 , Traspasos."FechaIngreso"
	 , Traspasos."FechaEgreso"
	 , Traspasos."FechaModificacion"
	 , Traspasos."TipoMovimiento"
	 , Traspasos."IdPlan"
	 , Traspasos."NombrePlan"
	 , Traspasos."CodigoRazonRetiro"
	 , Traspasos."RazonRetiro"
	 , Traspasos."Dias"  --****  (FECHA ACTUAL) - (FECHA DE INGRESO + DiAS VIGENCIA) *** 
	 , Traspasos."CodigoTipoTraspaso"
	 , Traspasos."IdCliente"
	 , Traspasos."IdCategoriaInversionista"
	 , Traspasos."Trabajador"
	 , Traspasos."DocumentoIdentidadDefecto"
	 , Traspasos."IdSocioNegocioCli"
	 , Traspasos."IdPersonaCli"
	 , Traspasos."NombreCompleto"
	 , Traspasos."IdPersona"
	 , Traspasos."IdSocioNegocioEMP"
	 , Traspasos."RazonSocial"
	 , Traspasos."IdEmpleador"
	 , Traspasos."CodigoEntidad"
	 , Traspasos."CodigoTipoAhorro"	
	 , Traspasos."NombreEntidad"
	 , Traspasos."DocumentoEntidad"
	 , Traspasos."Descripcion"
	 , Traspasos."IdEstado" 
	 , OrdenTraspaso."MontoOrdenTraspaso"
	 , Traspasos."EsADC"	
	 , Traspasos."Canal"
	 , Traspasos."DescripcionCanal"
	 , Estados."Nombre"
	 , Estados."Codigo"
	 , Traspasos."IdEstadoContrato";
GRANT SELECT ON "BSCL.APV.BD.Vistas::TRA.ConsultarTraspasosAPV" TO  BCS_DESARROLLADOR WITH GRANT OPTION;


