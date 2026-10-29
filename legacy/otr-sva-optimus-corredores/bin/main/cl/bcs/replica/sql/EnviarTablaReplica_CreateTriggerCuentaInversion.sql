CREATE TRIGGER "BSCL.Plataforma.BD::PLA.Replica.TriggerReplicaUpdateTablaCuentaInversion_SaldoFechaMovimiento"
 AFTER   UPDATE ON "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento"
 REFERENCING NEW ROW NewRow FOR EACH ROW 
 BEGIN
	INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
	SELECT cast(:NewRow."IdCargoAbonoMovimiento" AS varchar(200)),'CuentaInversion_MovimientosCuentaInversion', 'A', 0,  CURRENT_TIMESTAMP  FROM DUMMY;				
 END;