SET SCHEMA "BSCL";
GO 
DROP VIEW   "BSCL.Facturacion.BD.Vistas::CuadraturaTransaccionFacturacion";
GO 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::CuadraturaTransaccionFacturacion" AS

	SELECT	
		Trans."IdTransaccion", 
		Trans."FolioTransaccion", 
		Max(Trans."FechaSistemaCreacion") As "FechaTransaccion",
		Max(Trans."FechaOperacionTransaccion") As "FechaHoraTransaccion",
		Max(Trans."IdClasificacionTipoInstrumento") As "IdClasificacionTipoInstrumento",
		Max(Trans."IdTipoInstrumento") As "IdTipoInstrumento",       
			Max(Bolsa."Nombre") As "NombreBolsa",             
		Max(TipOpera."Nombre") As "TipoOperacion",
		Max(Trans."IdCondicionDeLiquidacion") As "IdCondicionLiquidacion", 
		Max(Trans."IdMonedaTransaccion") As "IdMoneda",
		Max(TipIns."Nombre") As "NombreTipoInstrumento",
		Case 
		  When Max(Trans."IdTipoInstrumento") in ('PRT', 'EMP', 'SIM','RRV','RRF') Then Max(Trans."InstrumentoSubyacente")
			ELSE Max(Trans."Instrumento")
		END As "NemotecnicoInstrumento",
		Max(Trans."CantidadTransaccion") As "Cantidad", 
		Max(Trans."PrecioTransaccion") As "Precio", 
		Max(Trans."MontoTransaccion") As "Monto",
		
		CASE WHEN max(Trans."IdClasificacionTipoInstrumento") = 'IF' THEN 
				Coalesce(sum( Case When Coalesce(MvtoCmbte."IdTipoComprobante",'') in ('FAC', 'FAE', 'POL','') 
									THEN Asig."ValorRescateAsignado" - Case When Coalesce(MvtoCmbteRef."IdTipoComprobante",'') = 'NCE' 
																						Then MvtoCmbteRef."ValorRescate" Else 0 END 
								Else 0 End), 0) 
		ELSE 
			Coalesce(sum( Case 
		                When Coalesce(MvtoCmbte."IdTipoComprobante",'') in ('FAC', 'FAE', 'POL','') THEN Asig."CantidadAsignada" - Case 
		                                                                                                             When Coalesce(MvtoCmbteRef."IdTipoComprobante",'') = 'NCE' Then MvtoCmbteRef."Cantidad" 
		                                                                                                           Else 0 
		                                                                                                           END 
		              Else 0
		              End), 0) 
		END AS "CantidadAsignada",


		CASE WHEN max(Trans."IdClasificacionTipoInstrumento") = 'IF' THEN 
			max(Trans."ValorRescateAsignadoTransaccion") - Coalesce(sum(Case When coalesce(MvtoCmbte."IdTipoComprobante",'') in ('FAC', 'FAE', 'POL','') 
																						Then Asig."ValorRescateAsignado" - Case  When COALESCE(MvtoCmbteRef."IdTipoComprobante",'') = 'NCE' 
																																	Then MvtoCmbteRef."ValorRescate" Else 0 
		                                                                                                                            End
																				Else 0
																				End),0)
		ELSE
			max(Trans."CantidadTransaccion") - Coalesce(sum(Case 
					                                                  When coalesce(MvtoCmbte."IdTipoComprobante",'') in ('FAC', 'FAE', 'POL','') Then Asig."CantidadAsignada" - Case
					                                                                                                                                               When COALESCE(MvtoCmbteRef."IdTipoComprobante",'') = 'NCE' Then MvtoCmbteRef."Cantidad" 
					                                                                                                                                             Else 0 
					                                                                                                                                             End
									Else 0
									End),0)	
		END AS "CantidadPorAsignar",
		
		CASE WHEN max(Trans."IdClasificacionTipoInstrumento") = 'IF' THEN
			Coalesce(sum(Case 
			               When Coalesce(MvtoCmbte."IdTipoComprobante",'') in ('FAC', 'FAE', 'POL','') Then MvtoCmbte."ValorRescate" - Case 
			                                                                                                         When COALESCE(MvtoCmbteRef."IdTipoComprobante",'') = 'NCE' Then MvtoCmbteRef."ValorRescate" 
			                                                                                                       Else 0 
			                                                                                                       End
						 Else 0
						 End),0)		
		ELSE
			Coalesce(sum(Case 
			               When Coalesce(MvtoCmbte."IdTipoComprobante",'') in ('FAC', 'FAE', 'POL','') Then MvtoCmbte."Cantidad" - Case 
			                                                                                                         When COALESCE(MvtoCmbteRef."IdTipoComprobante",'') = 'NCE' Then MvtoCmbteRef."Cantidad" 
			                                                                                                       Else 0 
			                                                                                                       End
						 Else 0
						 End),0) 
		END AS "CantidadEnviada",
					 
		CASE WHEN max(Trans."IdClasificacionTipoInstrumento") = 'IF' THEN
			max(Trans."ValorRescateTransaccion") - Coalesce(sum(Case 
				                          				When Coalesce(MvtoCmbte."IdTipoComprobante",'') in ('FAC', 'FAE', 'POL','') Then MvtoCmbte."ValorRescate"
				                          				- Case 
		                                                When COALESCE(MvtoCmbteRef."IdTipoComprobante",'') = 'NCE' THEN MvtoCmbteRef."ValorRescate"
		                                              Else 0 
		                                              End
																Else 0
																End),0)
		ELSE
			max(Trans."CantidadTransaccion") - Coalesce(sum(Case 
				                          				When Coalesce(MvtoCmbte."IdTipoComprobante",'') in ('FAC', 'FAE', 'POL','') Then MvtoCmbte."Cantidad" - Case 
		                                                When COALESCE(MvtoCmbteRef."IdTipoComprobante",'') = 'NCE' THEN MvtoCmbteRef."Cantidad" 
		                                              Else 0 
		                                              End
																Else 0
																End),0) 
		END AS "CantidadPorEnviar",		
														
		CASE WHEN max(Trans."IdClasificacionTipoInstrumento") = 'IF' THEN	
			Coalesce(Sum(Case 
			               When Coalesce(MvtoCmbte."IdTipoComprobante",'') in ('FAC', 'FAE', 'POL','') and COALESCE(MvtoCmbteRef."IdTipoComprobante",'') NOT IN ('NCR','NCE') Then Cmbte."ValorRescate" 
						 Else 0
						 End) - (Sum(Case 
						               When MvtoCmbte."IdTipoComprobante" in ('NCR', 'NCE') Then Cmbte."ValorRescate"
									 Else 0
									 End)),0)
		ELSE
			Coalesce(Sum(Case 
			               When Coalesce(MvtoCmbte."IdTipoComprobante",'') in ('FAC', 'FAE', 'POL','') and COALESCE(MvtoCmbteRef."IdTipoComprobante",'') NOT IN ('NCR','NCE') Then Cmbte."Cantidad" 
						 Else 0
						 End) - (Sum(Case 
						               When MvtoCmbte."IdTipoComprobante" in ('NCR', 'NCE') AND MvtoCmbteRef."IdTipoComprobante" IS NOT NULL Then Cmbte."Cantidad"
									 Else 0
									 End)),0) 
		END AS "CantidadFacturada",
								 
		CASE WHEN max(Trans."IdClasificacionTipoInstrumento") = 'IF' THEN
			max(Trans."ValorRescateTransaccion") - Coalesce(Sum(Case 
				                                                  When Coalesce(MvtoCmbte."IdTipoComprobante",'') in ('FAC', 'FAE', 'POL','') and COALESCE(MvtoCmbteRef."IdTipoComprobante",'') NOT IN ('NCR','NCE') Then Cmbte."ValorRescate"
																Else 0
																End) - (Sum(Case 
																              When MvtoCmbte."IdTipoComprobante" in ('NCR', 'NCE') Then Cmbte."ValorRescate"
																			Else 0
																			End)),0)												
		ELSE 
			max(Trans."CantidadTransaccion") - Coalesce(Sum(Case 
				                                                  When Coalesce(MvtoCmbte."IdTipoComprobante",'') in ('FAC', 'FAE', 'POL','') and COALESCE(MvtoCmbteRef."IdTipoComprobante",'') NOT IN ('NCR','NCE') Then Cmbte."Cantidad"
																Else 0
																End) - (Sum(Case 
																              When MvtoCmbte."IdTipoComprobante" in ('NCR', 'NCE') AND MvtoCmbteRef."IdTipoComprobante" IS NOT NULL Then Cmbte."Cantidad"
																			Else 0
																			End)),0)
		END AS "CantidadPorFacturar",
							
																	
	    Coalesce(Sum(Case 
	                  When MvtoCmbte."IdTipoComprobante" in ('FAC', 'FAE', 'POL') Then Cmbte."Monto"
					Else 0
					End) - (Sum(Case 
					              When MvtoCmbte."IdTipoComprobante" in ('NCR', 'NCE') Then Cmbte."Monto"
								Else 0
								End)),0) As "MontoFacturado",
		max(ROUND(Trans."MontoTransaccion", 0)) - Coalesce(Sum(Case 
		                                               When MvtoCmbte."IdTipoComprobante" in ('FAC', 'FAE', 'POL') Then ROUND(Cmbte."Monto", 0)
													 Else 0
													 End) - (Sum(Case 
													               When MvtoCmbte."IdTipoComprobante" in ('NCR', 'NCE') Then ROUND(Cmbte."Monto", 0)
																 Else 0
																 End)),0) As "MontoPorFacturar",
		CASE 
		  WHEN COALESCE(MAX(TransHis."IdTransaccion"),0) != 0 THEN 'PENDIENTE' 
		ELSE '' 
		END AS "TienePendientes",	
		Max(Orden."NombreCompleto") AS "NombreCliente",
		Max(Orden."DocumentoIdentidad") AS "DocumentoIdentidadCliente",
		Max(Portafolio."IdPortafolio") AS "IdPortafolioCliente",
		Max(Portafolio."CodigoPortafolio") AS "CodigoPortafolioCliente",
		Max(Portafolio."Alias") AS "NombrePortafolio",
		CASE 
		  WHEN Max(Orden."DocumentoIdentidad") = Max(DocIdentCtaProp."DocumentoIdentidad") THEN 'CTA.PROPIA'
		  WHEN Max(IndTipPersona."ValorIndicador") IN ('1','2','3','4','5') THEN 'RELACIONADOS' 
		  WHEN Max(IndTipPersona."ValorIndicador") = '6' THEN 'TERCEROS'
		ELSE 'TERCEROS'
		END AS "TipoCliente"														 
FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccion"  AS Trans
  LEFT JOIN "BSCL.Orden.BD::ORD.Asignacion" AS Asig ON Trans."IdTransaccion" = Asig."IdTransaccion" AND Coalesce(Asig."IdEstadoAsignacion",'') <> 'ANU'
  LEFT JOIN "BSCL.Orden.BD::ORD.Orden" AS Orden ON Orden."IdOrden" = Asig."IdOrden"
  LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = Orden."IdPortafolioCliente" AND Portafolio."IdEstado" = 'ACT'
  LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cliente ON Cliente."IdCliente" = Portafolio."IdCliente" AND Cliente."IdEstado" = 'ACT'
  LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" AS IndTipPersona ON IndTipPersona."IdEntidad" = Cliente."IdPersona" AND IndTipPersona."IdTipoIndicador" = 1027
  LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MvtoCmbte ON MvtoCmbte."IdAsignacion" = Asig."IdAsignacion" AND Coalesce(Asig."IdEstadoAsignacion",'') = 'EAF' AND MvtoCmbte."IdEstadoFacturacion" <> 'ANU'
  LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MvtoCmbteRef ON (MvtoCmbte."IdComprobante" = MvtoCmbteRef."IdComprobanteReferencia"  AND MvtoCmbteRef."IdComprobanteReferencia" > 0 AND MvtoCmbteRef."IdTipoComprobante" IN ('NCR','NCE') AND MvtoCmbteRef."IdEstadoFacturacion" <> 'ANU') 
  LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" AS Cmbte ON (Cmbte."IdMovimientoComprobante" = MvtoCmbte."IdMovimientoComprobante" AND Cmbte."IdTransaccion" = Trans."IdTransaccion")
  LEFT JOIN "BSCL.Plataforma.BD::INS.Bolsa" AS Bolsa ON Bolsa."IdBolsa" = Trans."IdBolsa"
  INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TipIns ON TipIns."IdTipoInstrumento" = Trans."IdTipoInstrumento" 
  LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion"	AS TipOpera ON TipOpera."IdTipoOperacion" = Trans."IdTipoOperacion"  AND TipOpera."IdTipoInstrumento" = Trans."IdTipoInstrumento"
  LEFT JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" AS TipCmbte ON TipCmbte."IdTipoComprobante" = MvtoCmbte."IdTipoComprobante"
  LEFT JOIN "BSCL.Orden.BD::HIS.Transaccion" AS TransHis ON TransHis."IdTransaccion" = Trans."IdTransaccion" AND TransHis."IdEstadoRegistro" = 'PEN'
  LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" AS Empresa ON Empresa."IdEmpresa" = Trans."IdEmpresa" AND Empresa."IdEstado" = 'ACT'
  LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SocNegCtaProp ON SocNegCtaProp."IdSocioDeNegocio" = Empresa."IdSocioDeNegocio"
  LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PerCtaProp ON PerCtaProp."IdPersona" = SocNegCtaProp."IdPersona" AND PerCtaProp."IdEstado" = 'ACT'
  LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocIdentCtaProp ON DocIdentCtaProp."IdDocumentoIdentidad" = PerCtaProp."IdDocumentoIdentidad" AND DocIdentCtaProp."IdEstado" = 'ACT'
WHERE Coalesce(Trans."IdEstadoTransaccion", '') <> 'ANU'
GROUP BY Trans."IdTransaccion", Trans."FolioTransaccion"
ORDER BY  Trans."IdTransaccion", Trans."FolioTransaccion";
GO

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::CuadraturaTransaccionFacturacion" TO BCS_DESARROLLADOR WITH GRANT OPTION;
GO 
