UPDATE "BSCL.APV.BD::TRA.Traspaso" 
SET "FechaModificacion" =:FechaModificacion
	,"NombreEntidad" =:NombreEntidad
	,"DocumentoEntidad" =:DocumentoEntidad
	,"CodigoTipoTraspaso" =:TipoTraspaso
	--,"PorcentajeTraspaso" =:PorcentajeTraspaso
	--,"MontoTraspaso" =:MontoTraspaso
WHERE "IdTraspaso" =:IdTraspaso