SELECT  
	TB."IdBanco" , 
	TB."IdTipoTarjetaBancaria" 	AS "IdTipo",
	TB."NumeroTarjetaBancaria" 	AS "NumeroTarjeta",
	--TB."CodigoVerificacion" 	AS "CodigoVerificacion",
	TO_BIGINT(TB."CodigoVerificacion")	AS "CodigoVerificacion",
	--TB."FechaVencimiento" 		AS "FechaVencimiento"
	TO_NVARCHAR(TB."FechaVencimiento", 'YYYY-MM-DD') AS "FechaVencimiento"
FROM "BSCL.SocioNegocio.BD::PER.TarjetaBancaria" AS TB 
WHERE TB."IdPersona" IN (:IdPersona);