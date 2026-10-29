SET SCHEMA "BSCL";

DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalClienteAnalitica";

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalClienteAnalitica" AS 
select 
						  EVTCLI."IdEventoCapitalCliente"
						 ,CASE WHEN IFNULL(EVTCLI."FechaHoraIngresoRegistro",'')= '' THEN '0001-01-01 00:00:00' 
					 	                              ELSE  
					 	                                  CASE WHEN  EVTCLI."FechaHoraIngresoRegistro" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
					 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (EVTCLI."FechaHoraIngresoRegistro", CONFIGURACION."Valor")),27)
					 	                                  END 
	  				      END AS "FechaHoraIngresoRegistro"
	  				    ,CASE WHEN IFNULL(EVTCLI."FechaHoraUltimaActualizacion",'')= '' THEN '0001-01-01 00:00:00' 
					 	                              ELSE  
					 	                                  CASE WHEN  EVTCLI."FechaHoraUltimaActualizacion" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
					 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (EVTCLI."FechaHoraUltimaActualizacion", CONFIGURACION."Valor")),27)
					 	                                  END 
	  				      END  AS "FechaHoraUltimaActualizacion"
						,CASE WHEN EVTCLI."IdEventoCapital" = 0 THEN NULL ELSE EVTCLI."IdEventoCapital" END AS "IdEventoCapital" 
						,CASE WHEN EVTCLI."IdTipoEventoCapital" = '' THEN NULL ELSE EVTCLI."IdTipoEventoCapital" END AS "IdTipoEventoCapital" 
						,CAST(TEVTCAPITAL."Nombre" AS NVARCHAR(100)) as "NombreEventoCapital"
						,EVTCLI."Secuencia"
						,LEFT(TO_CHAR(IFNULL(EVTCLI."FechaCierre",'0001-01-01')),10) AS "FechaCierre"
						,LEFT(TO_CHAR(IFNULL(EVTCLI."FechaPago",'0001-01-01')),10) AS "FechaPago"
						,CASE WHEN EVTCLI."IdMoneda" = '' THEN NULL ELSE EVTCLI."IdMoneda" END AS "IdMoneda" 
						,CASE WHEN EVTCLI."IdTipoOperacionGarantia" = '' THEN NULL ELSE EVTCLI."IdTipoOperacionGarantia" END AS "IdTipoOperacionGarantia" 
						,EVTCLI."Origen"
						,CAST(EVT."Descripcion" AS NVARCHAR(100)) AS "Descripcion"
						,CAST(coalesce(POR."Proposito",'') AS NVARCHAR(100)) as "Proposito"
						,CASE WHEN EVTCLI."IdSocioNegocio" = 0 THEN NULL ELSE EVTCLI."IdSocioNegocio" END AS "IdSocioNegocio" 
						,CAST(EVTCLI."TipoSocioNegocio" AS NVARCHAR(3)) AS "TipoSocioNegocio"
						,CASE WHEN POR."IdCliente" = 0 THEN NULL ELSE  POR."IdCliente" END as "IdCliente"
						,CAST(EVTCLI."NombreCliente" AS NVARCHAR(302)) AS "NombreCliente"
						,EVTCLI."IdTipoDocumentoIdentidad"
						,CAST(EVTCLI."DocumentoIdentidad" AS NVARCHAR(40)) AS "DocumentoIdentidad"
						,CASE WHEN EVTCLI."IdPortafolio" = 0 THEN NULL ELSE EVTCLI."IdPortafolio" END  as "IdPortafolio"
						,CAST(coalesce(POR."CodigoPortafolio",'') AS NVARCHAR(100)) as "CodigoPortafolio"
						,CAST(coalesce(POR."DescripcionPortafolio",'') AS NVARCHAR(100)) as "DescripcionPortafolio"
						,CASE WHEN EVTCLI."IdClasificacionTipoInstrumento" = '' THEN NULL ELSE EVTCLI."IdClasificacionTipoInstrumento" END AS "IdClasificacionTipoInstrumento" 
						,CASE WHEN EVTCLI."IdTipoInstrumento" = '' THEN NULL ELSE EVTCLI."IdTipoInstrumento" END AS "IdTipoInstrumento" 
						,CASE WHEN EVTCLI."IdInstrumento" = 0 THEN NULL ELSE EVTCLI."IdInstrumento" END AS "IdInstrumento" 
						,CAST(EVTCLI."Nemo" AS NVARCHAR(40)) AS "Nemo" 
						,CASE WHEN EVTCLI."IdInstrumentoNuevo" = 0 THEN NULL ELSE EVTCLI."IdInstrumentoNuevo" END AS "IdInstrumentoNuevo" 
						,CAST(EVTCLI."NemoNuevo" AS NVARCHAR(40)) AS "NemoNuevo"
						,EVTCLI."TipoCartera"
						,CASE WHEN RTRIM(EVTCLI."IdBoveda") = '' THEN 'COR' ELSE EVTCLI."IdBoveda" END AS "IdBoveda" 
						,CASE WHEN RTRIM(EVTCLI."IdBolsa") = '' THEN 'BCS' ELSE EVTCLI."IdBolsa" END AS "IdBolsa" 
						,EVTCLI."MontoReajuste"
						,EVTCLI."Precio"
						,EVTCLI."PrecioNuevo"
						,EVTCLI."NumeroDividendo"
						,EVTCLI."Paridad"
						,EVT."Factor"
						,EVT."Valor"
						,EVTCLI."CantidadDerecho"
						,EVTCLI."CantidadNueva"
						,EVTCLI."MontoCapital"
						,EVTCLI."MontoInteres"
						,EVTCLI."Total"
						,EVTCLI."SecuenciaCupon"
						,case when EVTCLI."IdConceptoGarantia" = '' then null else EVTCLI."IdConceptoGarantia" end as "IdConceptoGarantia"
						,EVTCLI."IdUsuario" 
						,CAST(coalesce(MON."Nombre",'') AS NVARCHAR(100)) AS "MonedaEvento"
						,CAST(EVTCLI."CodigoUsuarioAgente" AS NVARCHAR(40)) AS "CodigoUsuarioAgente"
						,CAST(coalesce(EMP."NombreCompleto",'') AS NVARCHAR(302)) as "NombreAgente"
						,CAST(coalesce(SUC."CodigoSucursal",'') AS NVARCHAR(40)) as "CodigoSucursal"
						,CASE WHEN EVTCLI."IdEstado" = '' THEN NULL ELSE EVTCLI."IdEstado" END AS "IdEstado" 
						,CAST(ESTADO."Descripcion" AS NVARCHAR(100)) as "NombreEstado"
						,EVTCLI."MontoGastoTransferencia"
						,EVTCLI."ImpuestoDivExtranjero"
						,EVTCLI."MontoImpuestoExtranjero"
						from "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EVTCLI
						inner join "BSCL.Custodia.BD::CUS.EventoCapital" AS  EVT
						on EVT."IdEventoCapital" = EVTCLI."IdEventoCapital"
						CROSS JOIN (select top 1 "Valor" from "BSCL.Plataforma.BD::PLA.Configuracion"
			            where "CodigoParametro" = 'PLA.SegundosZonaHoraria') AS CONFIGURACION 
						left outer join "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS POR
						on POR."IdPortafolio" = EVTCLI."IdPortafolio"
						left outer join "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda" AS MON
						on EVTCLI."IdMoneda" = MON."IdMoneda"
						inner join "BSCL.Custodia.BD.Vistas::CUS.ConsultarTipoEventoCapital"  AS TEVTCAPITAL
						on TEVTCAPITAL."IdTipoEventoCapital" = EVTCLI."IdTipoEventoCapital"
						inner join "BSCL.Plataforma.BD.Vistas::INS.ConsultarClasificacionTipoInstrumento" AS TIPOINST
						on TIPOINST."IdClasificacionTipoInstrumento" = EVTCLI."IdClasificacionTipoInstrumento"
						and TIPOINST."IdTipoInstrumento" = EVTCLI."IdTipoInstrumento"
						inner join "BSCL.Custodia.BD::CUS.EstadoCustodia" AS ESTADO
						on EVTCLI."IdEstado" = ESTADO."IdEstado"
						and ESTADO."NombreEntidad" = 'CUSTODIA.EVENTO'
						left outer join "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda" AS MONLIQ
						on MONLIQ."IdMoneda" = EVT."IdMoneda" 
						left outer join "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda" AS MONSRT
						on MONSRT."IdMoneda" = EVT."GlosaMonedaSorteoLetras"
						left join  "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AS EMP
						on EMP."IdEmpleado" = POR."IdEmpleadoEjecutivo" 
						left join "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSucursal" AS SUC
						on SUC."IdSucursal" = EMP."IdSucursal"; 

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalClienteAnalitica" TO BCS_DESARROLLADOR WITH GRANT OPTION;