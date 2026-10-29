INSERT INTO "BSCL.APV.BD::TRA.DetalleOrigenTraspaso"
(
 	 "IdDetalleOrigenTraspaso"	--BIGINT		 NOT NULL
	,"IdTraspaso"				--BIGINT		 NOT NULL
	,"CodigoTipoAhorro"			--NVARCHAR(10)
	,"IdEntidadRecaudadora"		--BIGINT
	,"NombreEntidad"			--NVARCHAR(100)
	,"DocumentoEntidad"			--NVARCHAR(11)
	,"Fondo"					--NVARCHAR(10)
	,"MontoTraspaso"			--DECIMAL(26,8)
	,"UFTraspaso"				--DECIMAL(26,8)
	,"PorcentajeTraspaso"		--DECIMAL(26,8)
	,"IdEstado"					--NVARCHAR(3)
	,"IdInstrumento"			
	,"IdPortafolio"					
	,"CodigoRegimenTributario"					
)
VALUES(
 :IdDetalleOrigenTraspaso
,:IdTraspaso
,:CodigoTipoAhorro
,:IdEntidadRecaudadora
,:NombreEntidad
,:DocumentoEntidad
,:Fondo
,:MontoTraspaso
,:UFTraspaso
,:PorcentajeTraspaso
,:IdEstado
,:IdInstrumento
,:IdPortafolio
,:CodigoRegimenTributario
);