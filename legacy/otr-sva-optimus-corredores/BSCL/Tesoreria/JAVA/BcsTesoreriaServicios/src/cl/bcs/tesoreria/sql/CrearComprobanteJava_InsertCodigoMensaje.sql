    INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_TablaCodigosMensajes"
	SELECT "NumeroReferencia" AS "IdInterno","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2","Resultado" 
	  FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Salida_Resultado"
	 WHERE "Resultado" <> 'OK';