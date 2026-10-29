SELECT distinct "b"."IdentServicio" , "a"."MontoCargo" as "MontoDeposito", "b"."IdCliente", "b"."IdPortafolio"
FROM "BSCL.Tesoreria.BD::CAR.CargaMasivaDetallePAC" as "a",  
	  "BSCL.Tesoreria.BD::PAC.MandatoPAC"  as "b"
WHERE "b"."IdentServicio" =  "a"."IdentServicio" and 
"a"."Estado" = 'A' and "a"."TipoArchivo" = 'REN';    