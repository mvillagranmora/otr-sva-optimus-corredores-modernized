CREATE TRIGGER "BSCL.Plataforma.BD::PLA.Replica.TriggerReplicaUpdateTablaCartera"
 AFTER   UPDATE ON "BSCL.Custodia.BD::CUS.Cartera"
 REFERENCING NEW ROW NewRow FOR EACH ROW 
 BEGIN
 	INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
	SELECT cast(:NewRow."IdCartera" AS varchar(200)),
		CASE :NewRow."IdClasificacionTipoInstrumento" 
			WHEN 'FM' THEN 'Custodia_CarteraFM' 
			WHEN 'RF' THEN 'Custodia_CarteraRFIF'
			WHEN 'IF' THEN 'Custodia_CarteraRFIF'
			WHEN 'RV' THEN 'Custodia_CarteraRV'
			ELSE 'Custodia_CarteraOtrosProductos' END,
	CASE WHEN :NewRow."IdEstado" = 'VIG' THEN 'A' else 'D' END , 0,  CURRENT_TIMESTAMP 
	FROM DUMMY;
END;