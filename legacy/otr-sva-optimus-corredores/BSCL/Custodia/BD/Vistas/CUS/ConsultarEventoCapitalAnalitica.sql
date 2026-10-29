SET SCHEMA "BSCL";

DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalAnalitica";

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalAnalitica" AS 
select 
					 CUS."IdEmpresa"
					,CUS."IdEventoCapital"
					,CASE WHEN IFNULL(CUS."FechaHoraIngresoRegistro",'')= '' THEN '0001-01-01 00:00:00' 
					 	                              ELSE  
					 	                                  CASE WHEN  CUS."FechaHoraIngresoRegistro" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
					 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (CUS."FechaHoraIngresoRegistro", CONFIGURACION."Valor")),27)
					 	                                  END 
	  				 END  as "FechaHoraIngresoRegistro"
	  				,CASE WHEN IFNULL(CUS."FechaHoraUltimaActualizacion",'')= '' THEN '0001-01-01 00:00:00' 
					 	                              ELSE  
					 	                                  CASE WHEN  CUS."FechaHoraUltimaActualizacion" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
					 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (CUS."FechaHoraUltimaActualizacion", CONFIGURACION."Valor")),27)
					 	                                  END 
	  				 END  as "FechaHoraUltimaActualizacion"
					,CASE WHEN CUS."IdTipoEventoCapital" = '' THEN NULL ELSE CUS."IdTipoEventoCapital" END AS "IdTipoEventoCapital" 
					,CAST(TEVT."Nombre" AS NVARCHAR(100)) as "NombreEventoCapital"
					,CAST(CUS."Descripcion" AS NVARCHAR(100)) AS "Descripcion" 
					,CUS."Secuencia"
					,CASE WHEN CUS."IdMoneda" = '' THEN 'CLP' ELSE CUS."IdMoneda" END AS "IdMoneda" 
					,coalesce(CAST(MON."Nombre" AS NVARCHAR(100)),'PESO CHILENO') as "NombreMoneda"
					,CUS."Paridad"
					,CASE WHEN CUS."IdEmisor" = '' THEN NULL ELSE CAST(CUS."IdEmisor" AS NVARCHAR(40)) END AS "IdEmisor"
					,CASE WHEN CUS."IdClasificacionTipoInstrumento" = '' THEN NULL ELSE CUS."IdClasificacionTipoInstrumento" END AS "IdClasificacionTipoInstrumento"
					,CASE WHEN CUS."IdTipoInstrumento" = '' THEN NULL ELSE CUS."IdTipoInstrumento" END AS "IdTipoInstrumento"
					,CASE WHEN CUS."IdInstrumento" = 0 THEN NULL ELSE CUS."IdInstrumento" END AS "IdInstrumento"
					,CAST(CUS."Nemo" AS NVARCHAR(40)) AS "Nemo" 
					,CUS."EsInscrito"
					,LEFT(TO_CHAR(IFNULL(CUS."FechaLimite",'0001-01-01')),10) AS "FechaLimite"
					,LEFT(TO_CHAR(IFNULL(CUS."FechaActualizacion",'0001-01-01')),10) AS "FechaActualizacion"
					,LEFT(TO_CHAR(IFNULL(CUS."FechaPago",'0001-01-01')),10) AS "FechaPago"
					,CASE WHEN CUS."IdMonedaLiquidacion" = '' THEN 'CLP' ELSE CUS."IdMonedaLiquidacion" END AS "IdMonedaLiquidacion" 
					,coalesce(CAST(MONLIQ."Nombre" AS NVARCHAR(100)), 'PESO CHILENO') as "NombreMonedaLiquidacion"
					,CUS."Factor"
					,CUS."Valor"
					,CASE WHEN CUS."IdEmisorNuevo" = '' THEN NULL ELSE CAST(CUS."IdEmisorNuevo" AS NVARCHAR(40)) END AS "IdEmisorNuevo"
					,CASE WHEN CUS."IdInstrumentoNuevo" = 0 THEN NULL ELSE CUS."IdInstrumentoNuevo" END AS "IdInstrumentoNuevo"
					,CAST(CUS."NemoNuevo" AS NVARCHAR(40)) AS "NemoNuevo" 
					,CUS."FactorNuevo"
					,CASE WHEN CUS."IdTipoDividendo" = '' THEN NULL ELSE CUS."IdTipoDividendo" END AS "IdTipoDividendo" 
					,CUS."NumeroDividendo"
					,CUS."EmisorSorteoLetras"
					,CUS."InstrumentoSorteoLetras"
					,CASE WHEN CUS."IdMoneda" = '' THEN NULL ELSE CUS."IdMoneda" END as "IdMonedaSorteoLetras"
					,CUS."BloqueoSorteoLetras"
					,CUS."SorteadoSorteoLetras"
					,CUS."APagarSorteoLetras"
					,CUS."APagarPesosSorteoLetras"
					,CUS."CuentaDepositanteSorteoLetras"
					,CUS."GlosaMonedaSorteoLetras"
					,CASE WHEN CUS."EmisorPagadorSorteoLetras" = 0 THEN NULL ELSE CUS."EmisorPagadorSorteoLetras" END AS "EmisorPagadorSorteoLetras" 
					,CUS."GlosaInstrumentoSorteoLetras"
					,CUS."Origen"
					,CUS."TipoIngreso"
					,CASE WHEN CUS."IdEstado" = '' THEN NULL ELSE CUS."IdEstado" END AS "IdEstado" 
					,CAST(ESTADO."Descripcion" AS NVARCHAR(100)) as "NombreEstado"
					,CASE WHEN CUS."IdUsuario" = 0 THEN NULL ELSE CUS."IdUsuario" END AS "IdUsuario" 
					,CUS."ImpuestoDivExtranjero"
					,CUS."GastoTransferencia"
					from "BSCL.Custodia.BD::CUS.EventoCapital" as CUS
					inner join "BSCL.Custodia.BD.Vistas::CUS.ConsultarTipoEventoCapital" as TEVT
						ON TEVT."IdTipoEventoCapital" = CUS."IdTipoEventoCapital"
					left join "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda"  as MON
						ON MON."IdMoneda" = CUS."IdMoneda"
					left join "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda"  as MONLIQ
						ON MONLIQ."IdMoneda" = CUS."IdMonedaLiquidacion"	
					inner join "BSCL.Custodia.BD::CUS.EstadoCustodia" AS ESTADO
						ON CUS."IdEstado" = ESTADO."IdEstado"
						and ESTADO."NombreEntidad" = 'CUSTODIA.EVENTO'	
					CROSS JOIN (select top 1 "Valor" from "BSCL.Plataforma.BD::PLA.Configuracion"
								            where "CodigoParametro" = 'PLA.SegundosZonaHoraria') AS CONFIGURACION;	

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalAnalitica" TO BCS_DESARROLLADOR WITH GRANT OPTION;