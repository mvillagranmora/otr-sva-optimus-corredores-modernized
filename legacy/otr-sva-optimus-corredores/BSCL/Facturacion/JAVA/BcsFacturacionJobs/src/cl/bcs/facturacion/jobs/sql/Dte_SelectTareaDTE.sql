Select   distinct "IdTareaDTE", 
         "TipoTareaDTE",
         ConsCmbte."IdRespuestaDTE",
         ConsCmbte."RespuestaDTEEnvioSII",
         ConsCmbte."RespuestaLocationDTE",
         ConsCmbte."RespuestaPdfDTE",
         ConsCmbte."RespuestaXmlDTE",
         "FolioComprobanteRespuestaDTE",
         --DENSE_RANK () OVER (ORDER by ConsCmbteDet."IdComprobante") 
         ConsCmbteDet."IdComprobante",
         'PEN' As "IdEstadoTareaDTE",
         ConsCmbte."IdEstadoDTE",
         ConsCmbte."IdEstadoSII",
         ConsCmbteDet."IdTipoComprobante" As "IdTipoComprobante",
         TipCmbte."TipoComprobanteDTE",                             
         ConsCmbteDet."FolioComprobante" As "FolioComprobante",
         ConsCmbteDet."IdEmpresa",
         ConsCmbteDet."AccionTipoOperacion" As "AccionTipoOperacion",                           
         ConsCmbteDet."FechaComprobante",
         ConsCmbte."FechaVencimiento",
         ConsTipOpe."GeneraOperacionPlazo",
         ----dias pago 
         
         ----Apb y saldo favor/contra
         Case
           When ((length(Inst."IdInstrumento") > 0 OR Inst."IdInstrumento" > 0) OR (length(MarkMak."IdInstrumento") > 0 OR MarkMak."IdInstrumento" > 0)) AND
                ConsCmbteDet."IdTipoComprobante" In ('FAC', 'POL', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'FOX') And ConsTipOpe."Accion" = 'COMPRA'  THEN 'APBCONTRA'                               
           When ((length(Inst."IdInstrumento") > 0 OR Inst."IdInstrumento" > 0) OR (length(MarkMak."IdInstrumento") > 0 OR MarkMak."IdInstrumento" > 0)) AND
                ConsCmbteDet."IdTipoComprobante" In ('FAC', 'POL', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'FOX') And ConsTipOpe."Accion" = 'VENTA' And (ConsCmbte."MontoLiquidacion" - Coalesce(ConsCmbte."MontoCobro", 0) > 0)  THEN 'APBFAVOR'
           When ((length(Inst."IdInstrumento") > 0 OR Inst."IdInstrumento" > 0) OR (length(MarkMak."IdInstrumento") > 0 OR MarkMak."IdInstrumento" > 0)) AND
                ConsCmbte."IdTipoComprobante" In ('FAC', 'POL', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'FOX') And ConsTipOpe."Accion" = 'VENTA' And (ConsCmbte."MontoLiquidacion" - Coalesce(ConsCmbte."MontoCobro", 0) < 0) Then 'APBCONTRA'
           When ConsCmbteDet."IdTipoComprobante" In ('FAC', 'POL', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'FOX') And ConsTipOpe."Accion" = 'COMPRA'  THEN 'APBCONTRA'                               
           When ConsCmbteDet."IdTipoComprobante" In ('FAC', 'POL', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'FOX') And ConsTipOpe."Accion" = 'VENTA' And (ConsCmbte."MontoLiquidacion" - Coalesce(ConsCmbte."MontoCobro", 0) > 0)  THEN 'APBFAVOR'
           When ConsCmbte."IdTipoComprobante" In ('FAC', 'POL', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'FOX') And ConsTipOpe."Accion" = 'VENTA' And (ConsCmbte."MontoLiquidacion" - Coalesce(ConsCmbte."MontoCobro", 0) < 0) Then 'APBCONTRA'     
         Else ''
         End As "CodigoReceptor",                             
         
         COALESCE(DoctoIndenEmisor."DocumentoIdentidadAsociado",DoctoIndenEmisor."DocumentoIdentidadAsociado") As "DocumentoIdentidadEmisor",
         COALESCE(DoctoIndenEmisor."IdTipoDocumentoIdentidadAsociado",DoctoIndenEmisor."IdTipoDocumentoIdentidadAsociado") As "TipoDocumentoIdentidadEmisor",
         Case
           When Length(ConsPer."RazonSocial") > 0 Then ConsPer."RazonSocial"
         Else ''
         End As "RazonSocialEmisor", 
         Case 
           When Length(GiroEmisor."Nombre") > 0 Then GiroEmisor."Nombre"
         Else 'En definicion'
         End As "GiroEmisor",
         Case
            When Length(IFNULL(ConsEmp."DireccionFisicaPrincipalCalle",'') || ' ' || IFNULL(Empresa."DireccionFisicaPrincipalNumero",'')) > 0 Then (IFNULL(ConsEmp."DireccionFisicaPrincipalCalle",'') || ' ' || IFNULL(Empresa."DireccionFisicaPrincipalNumero",''))
         Else 'En definicion'
         End AS "DireccionEmisor",
         Case
            When Length(ConsEmp."DireccionFisicaPrincipalNombreDivisionPais") > 0 Then ConsEmp."DireccionFisicaPrincipalNombreDivisionPais"
         Else 'En definicion'
         End AS "ComunaEmisor",                             
         Case
            When Length(ConsEmp."DireccionFisicaPrincipalNombreCiudad") > 0 Then ConsEmp."DireccionFisicaPrincipalNombreCiudad"
         Else 'En definicion'
         End AS "CiudadEmisor",                             
         Case
            When Length(ConsEmp."DireccionElectronicaPrincipal") > 0 Then ConsEmp."DireccionElectronicaPrincipal"
         Else 'En definicion'
         End AS "EmailEmisor",
         ConsCmbteDet."CodigoCondicionDeLiquidacion" AS "CondicionPago",
         ConsCmbte."Glosa" AS "Leyenda",
         
         
         ConsEmp."TelefonoPrincipal" As "TelefonoEmisor",                             
         /** Cliente **/
         DirElec."Direccion" As "EmailReceptor",
         COALESCE(ConsCli."TipoDocumentoIdentidad", ConsCli."TipoDocumentoIdentidad", '') As "TipoDocumentoIdentidadReceptor",
         COALESCE(ConsCli."DocumentoIdentidad", ConsCli."DocumentoIdentidad",'') As "DocumentoIdentidadReceptor",
         COALESCE(ConsCli."TipoDocumentoIdentidadAdicional", ConsCli."TipoDocumentoIdentidadAdicional", '') As "TipoDocumentoIdentidadAdicionalReceptor",
         COALESCE(ConsCli."DocumentoIdentidadAdicional",ConsCli."DocumentoIdentidadAdicional", '') As "DocumentoIdentidadAdicionalReceptor",
         COALESCE(DoctoInden."IdTipoDocumentoIdentidadAsociado", DoctoInden."IdTipoDocumentoIdentidadAsociado", '') As "TipoDocumentoIdentidadAsociado",
         COALESCE(DoctoInden."DocumentoIdentidadAsociado", DoctoInden."DocumentoIdentidadAsociado", '') As "DocumentoIdentidadAsociado",
         
         Portafolio."CodigoPortafolio" AS "CodigoSucursal",                             
         ConsCli."NombreCompleto" As "RazonSocialReceptor",
         Case 
           When Length(GiroReceptor."Nombre") > 0 Then GiroReceptor."Nombre"
         Else 'En definicion'
         End As "GiroReceptor",
         Case
           When Length(IFNULL(DirFis."Calle",'') || ' ' || IFNULL(DirFis."Numero",'') || ' ' || IFNULL(DirFis."Departamento",'')) > 0 Then (IFNULL(DirFis."Calle",'') || ' ' || IFNULL(DirFis."Numero",'') || ' ' || IFNULL(DirFis."Departamento",''))  
         Else 'En definicion' 
         End As "DireccionReceptor",
          Case
            When Length(ConsDivPais."NombreDivisionPais") > 0 Then ConsDivPais."NombreDivisionPais"
          Else 'En definicion'
          End As "ComunaReceptor",                              
          Case 
            When Length(Ciudad."NombreCiudad") > 0 Then Ciudad."NombreCiudad"
          Else 'En definicion'
          End As "CiudadReceptor", 
          ConsCmbteDet."CodigoCondicionDeLiquidacion" As "ContactoReceptor",   
          --AjusteDocumento
          'COMISION' As "GlosaAjusteComision",
          IFNULL(ConsCmbte."TotalComisCmbte",0) As "TotalComisionCmbte",
          'IMPUESTO' As "GlosaAjusteImpuesto",
          IFNULL(ConsCmbte."TotalImptoCmbte",0) As "TotalImpuestoCmbte",
          'DERECHO' As "GlosaAjusteDerecho",
          IFNULL(ConsCmbte."TotalDereCmbte",0) As "TotalDerechoCmbte",
          'GASTO' As "GlosaAjusteGasto",
          IFNULL(ConsCmbte."TotalGastoCmbte",0) As "TotalGastoCmbte",
          'RETENCION' As "GlosaAjusteRetencion",
          IFNULL(ConsCmbte."TotalRetencionCmbte",0) As "TotalRetencionCmbte",                              
          --ReferenciaDocumento
          ---
          'SAP' As "TipoReferenciaIdComprobante", 
          ConsCmbteDet."IdComprobante" As "FolioReferenciaIdComprobante",  
          '' As "FechaReferenciaIdComprobante",
          'Nro. Interno Optimus SAP' As "RazonReferenciaIdComprobante",
          ---
          'VIS' As "TipoReferenciaFolioComprobante", 
          ConsCmbteDet."FolioComprobante" As "FolioReferenciaFolioComprobante",  
          '' As "FechaReferenciaFolioComprobante",
          'Nro. Visible SAP' As "RazonReferenciaFolioComprobante", 
          ---Notas Debito / Credito
          0 AS "NumeroReferenciaNota",
          CASE 
            WHEN (ConsCmbteDet."IdTipoComprobante" = 'NCR' OR ConsCmbteDet."IdTipoComprobante" = 'NCE' OR ConsCmbteDet."IdTipoComprobante" = 'NEO') THEN Cast(TipCmbteNota."TipoComprobanteDTE" AS Nvarchar(10)) 
          ELSE ''
          END As "TipoReferenciaNota",
          CASE 
            WHEN (ConsCmbteDet."IdTipoComprobante" = 'NCR' OR ConsCmbteDet."IdTipoComprobante" = 'NCE' OR ConsCmbteDet."IdTipoComprobante" = 'NEO') THEN Cast(TipCmbteNota."TipoComprobanteDTEPE" AS Nvarchar(10)) 
          ELSE ''
          END As "TipoReferenciaNotaPE",
          TipCmbteNota."Codigo" AS "CodigoTipoReferenciaNotaPE",
          CASE 
            WHEN (ConsCmbteDet."IdTipoComprobante" = 'NCR' OR ConsCmbteDet."IdTipoComprobante" = 'NCE' OR ConsCmbteDet."IdTipoComprobante" = 'NEO') THEN ConsCmbteDetNota."FolioComprobante" 
          ELSE 0
          END As "FolioReferenciaNota",
          CASE 
            WHEN (ConsCmbteDet."IdTipoComprobante" = 'NCR' OR ConsCmbteDet."IdTipoComprobante" = 'NCE' OR ConsCmbteDet."IdTipoComprobante" = 'NEO') THEN ConsCmbteDetNota."FechaComprobante" 
          ELSE ''
          END As "FechaReferenciaNota",
          CASE 
            WHEN (ConsCmbteDet."IdTipoComprobante" = 'NCR' OR ConsCmbteDet."IdTipoComprobante" = 'NCE' OR ConsCmbteDet."IdTipoComprobante" = 'NEO') THEN ConsCmbteDet."Glosa" 
          ELSE ''
          END As "RazonReferenciaNota",
          CASE 
            WHEN (ConsCmbteDet."IdTipoComprobante" = 'NCR' OR ConsCmbteDet."IdTipoComprobante" = 'NCE' OR ConsCmbteDet."IdTipoComprobante" = 'NEO') THEN ConsCmbteDet."IdComprobanteReferencia" 
          ELSE 0
          END As "CodigoReferenciaNota",
          --DetalleDocumento                              
          CASE
            WHEN ConsCmbteDet."IdClasificacionTipoInstrumento" = 'RV' THEN (CASE
                                                                              WHEN (length(Inst."IdInstrumento") > 0 OR Inst."IdInstrumento" > 0) THEN (IfNull(ConsCmbteDet."NombreTipoOperacion" || ',', '') || IfNull(ConsCmbteDet."NemotecnicoInstrumento" || ',', '') || IfNull(ConsCmbteDet."IndicadorOrdenDirecta" || ',', '')  || 'APB')
                                                                              WHEN (length(MarkMak."IdInstrumento") > 0 OR MarkMak."IdInstrumento" > 0) THEN (IfNull(ConsCmbteDet."NombreTipoOperacion" || ',', '') || IfNull(ConsCmbteDet."NemotecnicoInstrumento" || ',', '') || IfNull(ConsCmbteDet."IndicadorOrdenDirecta" || ',', '') || 'APB')
                                                                            ELSE (IfNull(ConsCmbteDet."NombreTipoOperacion" || ',', '') || IfNull(ConsCmbteDet."NemotecnicoInstrumento" || ',', '') || IfNull(ConsCmbteDet."IndicadorOrdenDirecta", '') || IfNull(ConsCmbteDet."Glosa", ''))
                                                                            END)
            WHEN ConsCmbteDet."IdClasificacionTipoInstrumento" = 'DV' THEN (CASE
                                                                              WHEN (length(Inst."IdInstrumento") > 0 OR Inst."IdInstrumento" > 0) THEN (IfNull(ConsCmbteDet."NombreTipoOperacion" || ',', '') || IfNull(ConsCmbteDet."NemotecnicoInstrumento" || ',', '') || IfNull(ConsCmbteDet."IndicadorOrdenDirecta" || ',', '')  || 'APB' || IfNull(Inst."EsPutEsCall",'') || ' ' || IfNull(Inst."ModalidadLiquidacion",'') || ' ' || IfNull(Inst."PrecioStrike", 0))
                                                                            ELSE (IfNull(ConsCmbteDet."NombreTipoOperacion" || ',', '') || IfNull(ConsCmbteDet."NemotecnicoInstrumento" || ',', '') || IfNull(ConsCmbteDet."IndicadorOrdenDirecta", '') || IfNull(ConsCmbteDet."Glosa", '') || IfNull(Inst."EsPutEsCall",'') || ' ' || IfNull(Inst."ModalidadLiquidacion",'') || ' ' || IfNull(Inst."PrecioStrike", 0))
                                                                            END)
          ELSE
            (IfNull(ConsCmbteDet."NombreTipoOperacion" || ',', '') || IfNull(ConsCmbteDet."NemotecnicoInstrumento" || ',', '') || IfNull(ConsPer."RazonSocial" || ',', '') || IfNull(ConsCmbteDet."IndicadorOrdenDirecta", ''))
          END As "IdComprobanteDetalleGlosa",            
          ConsCmbteDet."AccionTipoOperacion" As "IdComprobanteDetalleDescripcion",                              
		  Case 
	   		When ConsCmbte."IdTipoInstrumento" in ('PRT', 'EMP', 'SIM','RRV','RRF') then ConsCmbteDet."NemotecnicoSubyacente" 
	   	  Else ConsCmbteDet."NemotecnicoInstrumento"
	      End As "IdComprobanteDetalleNemotecnico",
		  ConsCmbteDet."NemotecnicoSubyacente" As "IdComprobanteDetalleNemotecnicoSubyacente",
          
          ConsCmbteDet."NroLinea" As "IdComprobanteDetalleNumero",
          ConsCmbteDet."ComprobanteDetalleCantidad" As "IdComprobanteDetalleCantidad",
          ConsCmbteDet."ComprobanteDetallePrecio" As "IdComprobanteDetallePrecio", 
           0 As "IdComprobanteDetallePorcentaje",                               
           0 As "IdComprobanteDetalleMontoDescuento",
           ConsCmbteDet."MontoComprobanteDetalle" As "IdComprobanteDetalleMonto",
           Case
             When ConsCmbteDet."EsExento" = 'S' Then 1
           Else 0                                   
           End As "IdComprobanteDetalleEsExento",                               
           CASE 
             WHEN ConsCmbteDet."IdTipoComprobante" = 'BOE' THEN NULL
           END AS "IdComprobanteDetalleBoletaEditable",
           --Parametros Documentos
           ConsCmbte."NombreTipoProducto" As "ParametroKeyProducto",
           
           -- Perú
           TipCmbte."TipoComprobanteDTEPE",
           ConsCmbte."AccionTipoOperacion" AS "AccionTipoOperacion",
           Inst."IdMoneda" AS "MonedaTransaccion",
           ConsCmbte."IdMonedaLiquidacion" AS "MonedaLiquidacion",
           ConsCmbte."IdMonedaLiquidacion" AS "Moneda", --PARA NOTA DE CREDITO
           ConsCmbte."FechaLiquidacion" AS "FechaLiquidacion",
           ConsCmbte."EsAPlazo" AS "EsAPlazo",
           CASE
             WHEN ConsPrefFac."Valor" = 'S' THEN ProdCCB."ProductoIngles"
           ELSE ProdCCB."ProductoEspanol"
           END AS "ProductoOperacion",
           
           ConsPrefFac."Valor" AS "PolizaIngles",
           
           COALESCE(Pais."NombrePais",'') AS "NacionalidadReceptor",
           ConsCmbte."CantidadTotal" AS "CantidadTotal",
           
           ConsCmbte."Monto" As "ImporteTotal",
           ConsCmbte."MontoLiquidacion" AS "ImporteLiquidacion",
           Abs(ConsCmbte."MontoLiquidacionFinal") As "TotalLiquidacion",
           ConsCmbte."IdMonedaLiquidacion" As "MonedaImporte",
           MonedaLiq."NombrePlural" AS "NombreMonedaImporte",
           Abs(ConsCmbte."MontoLiquidacionFinal") As "ImporteAPagar",
           Inst."CorteMinimo",
           Inst."Tamano",
           ConsCmbteDet."IdClasificacionTipoInstrumento",
           ConsCmbteDet."IdConcepto",
           Concepto."Nombre" AS "NombreConcepto",
           ConceptoNota."Nombre" AS "NombreConceptoNota",
           ConsCmbteDet."NombreTipoOperacion",
           "ComisionBVLMonedaLiquidacion",
           "ComisionSABMonedaLiquidacion",
           "ComisionSMVMonedaLiquidacion",
           "ComisionCavaliMonedaLiquidacion",
           "FondoGarantiaMonedaLiquidacion",
           "FondoLiquidacionMonedaLiquidacion",
           "OtrasComisionesMonedaLiquidacion",
           "ImpuestoIGVMonedaLiquidacion",
           Coalesce(ConsCmbteDetNota."IdTipoComprobante", ConsCmbteDetNota."IdTipoComprobante", '') AS "IdTipoReferenciaNota",
           Coalesce(ConsCmbteDetNota."IdConcepto", ConsCmbteDetNota."IdConcepto", '') AS "IdConceptoNota",           
           Coalesce(ConsCmbteDetNota."Glosa", ConsCmbteDetNota."Glosa", '') AS "LeyendaNota",
           ConsCmbte."ValorTipoCambio",
           ConsCmbte."NombreBovedaCuentaCustodioPortafolio",
           ConsCtaCusPor."NombreTipoCustodia",
           ConsCtaCusPor."NombreCompletoBancoCustodio",
           ConsCtaCusPor."NombreCompletoBancoLiquidador",
           ConsCtaCusPor."Codigo" AS "CodigoDeCuenta",
           ConsCmbteDetNota."MontoComprobanteDetalle" As "IdComprobanteDetalleMontoNota",
           TipCmbteNota."Nombre" AS "TipoComprobanteNota",
           ConsCmbte."Intereses",
           ConsCmbte."Tasa",
           ConsCmbte."DiasPlazo",
           ConsFormCus."Nombre" AS "NombreFormaCustodia",
           COALESCE(ConsCmbte."ReajustesVAC",0) AS "ReajustesVAC",
           CASE 
           	WHEN IFNULL(TipCmbte."IdTipoComprobante",'') = 'FOE' AND IFNULL(Con."EsReembolso",'') = 'N'
           		THEN 'Operación sujeta al SPOT Banco de la Nación cta. 00000373443'
           		ELSE ''
           END 									AS "Observacion"
	FROM "BSCL.Facturacion.BD::FAC.TareaDTE" tarea 
	INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteConDetalle" As ConsCmbteDet ON tarea."IdComprobante" = ConsCmbteDet."IdComprobante"
	INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" As ConsCmbte On ConsCmbte."IdComprobante" = ConsCmbteDet."IdComprobante"
	LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobrosPeruTotalizados" cob ON ConsCmbte."IdComprobante" = cob."IdComprobante"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" Concepto ON Concepto."IdConcepto" = ConsCmbtedet."IdConcepto"
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConceptosManuales"	AS Con	ON (ConsCmbtedet."IdConcepto" = Con."IdConcepto")
    Inner Join "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" AS Empresa On Empresa."IdEmpresa" = ConsCmbteDet."IdEmpresa"
    Inner Join "BSCL.Facturacion.BD::FAC.TipoComprobante" As TipCmbte On TipCmbte."IdTipoComprobante" = ConsCmbteDet."IdTipoComprobante"  
    Inner Join "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" As ConsCli On ConsCli."IdCliente" = ConsCmbteDet."IdCliente"
    Inner Join "BSCL.Portafolio.BD::POR.Portafolio" As Portafolio On Portafolio."IdPortafolio" = ConsCmbteDet."IdPortafolio"                           
    Inner Join "BSCL.Plataforma.BD::INS.TipoOperacion" As ConsTipOpe On ConsTipOpe."IdTipoOperacion" = ConsCmbteDet."IdTipoOperacion"
    LEFT JOIN  "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DoctoInden ON DoctoInden."IdDocumentoIdentidad" = ConsCli."IdDocumentoIdentidad"
    Left Join "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" As ConsEmp On ConsEmp."IdEmpresa" = ConsCmbteDet."IdEmpresa"        
    Left Join "BSCL.SocioNegocio.BD::PER.Persona" As ConsPer On ConsPer."IdPersona" = ConsEmp."IdPersona"
    LEFT JOIN  "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DoctoIndenEmisor ON DoctoIndenEmisor."IdDocumentoIdentidad" = ConsEmp."IdDocumentoIdentidad"
    
    Left Join "BSCL.Plataforma.BD.Vistas::REG.ConsultarGiro" As GiroEmisor On GiroEmisor."IdGiro" = ConsPer."IdGiroPrincipal"   
    Left Join "BSCL.Plataforma.BD.Vistas::REG.ConsultarGiro" As GiroReceptor On GiroReceptor."IdGiro" = ConsCli."IdGiroPrincipal"
    Left Join "BSCL.SocioNegocio.BD::PER.DireccionFisica" As DirFis On DirFis."IdDireccionFisica" = ConsCli."IdDireccionFisicaPrincipal" 
    Left Join "BSCL.Plataforma.BD::REG.Ciudad" As Ciudad On Ciudad."IdCiudad" = DirFis."IdCiudad"
    Left Join "BSCL.Plataforma.BD.Vistas::REG.ConsultarDivisionPais" As ConsDivPais On ConsDivPais."IdDivisionPais" = DirFis."IdDivisionPais"
      And ConsDivPais."IdCiudad" = Ciudad."IdCiudad"         
	Left Join "BSCL.SocioNegocio.BD::PER.DireccionElectronica" As DirElec On DirElec."IdDireccionElectronica" = ConsCli."IdDireccionElectronicaPrincipal"
    Left Join "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteConDetalle" AS ConsCmbteDetNota On ConsCmbteDetNota."IdComprobante" = ConsCmbteDet."IdComprobanteReferencia"
    LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" ConceptoNota ON ConceptoNota."IdConcepto" = ConsCmbteDetNota."IdConcepto"
    Left Join "BSCL.Facturacion.BD::FAC.TipoComprobante" As TipCmbteNota On TipCmbteNota."IdTipoComprobante" = ConsCmbteDetNota."IdTipoComprobante"
    LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio" AS ConsCtaCusPor 
      ON ConsCtaCusPor."IdCuentaCustodioPortafolio" = ConsCmbte."IdCuentaCustodioPortafolio"
    LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Inst ON Inst."IdInstrumento" = ConsCmbteDet."IdInstrumento"
    
    LEFT JOIN "BSCL.Plataforma.BD::INS.MarketMaker" AS MarkMak On MarkMak."IdInstrumento" = ConsCmbteDet."IdInstrumento"
      AND ((ConsCmbteDet."FechaComprobante" >= MarkMak."FechaInicio") AND (ConsCmbteDet."FechaComprobante" <= MarkMak."FechaFin"))
      AND MarkMak."IdEstado" = 'ACT'
    LEFT JOIN "BSCL.Plataforma.BD.Vistas::REG.ConsultarPais" AS Pais 
      ON Pais."IdPais" = ConsCli."IdPaisResidencia"
    LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciasFacturacion" ConsPrefFac
      On ConsPrefFac."IdPortafolio" = ConsCmbteDet."IdPortafolio"
     And ConsPrefFac."IdCliente" = ConsCmbteDet."IdCliente"
     And ConsPrefFac."Codigo"='FAC_EMPOEN'
    LEFT JOIN "BSCL.Facturacion.BD::FAC.ProductoCCB" AS ProdCCB 
      On ProdCCB."IdClasificacionTipoInstrumento" = ConsCmbteDet."IdClasificacionTipoInstrumento"
     And ProdCCB."IdTipoProducto" = ConsCmbteDet."IdTipoProducto"
     And ProdCCB."IdBolsa" = ConsCmbteDet."IdBolsa"
    LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarFormaCustodiaOrden" AS ConsFormCus
     ON ConsFormCus."IdFormaCustodiaOrden" = ConsCmbte."IdFormaCustodia"
    LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MonedaLiq
     ON MonedaLiq."IdMoneda" = ConsCmbte."IdMonedaLiquidacion"
	WHERE "IdTareaDTEGrupo" = :IdTareaDTEGrupo
	
	Order by ConsCmbteDet."IdComprobante";
  
  
  