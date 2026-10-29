SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.REPO2P2.Datos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.REPO2P2.Datos" (

	"ID",
	"IdDocumento",
	"FuenteDatos",
    "Estado",
    "EstadoSICAM",
    "SICAM",
    "SICAMDescripcion",
--camos layout    
	"TipoRegistro",
	"TipoRegistroDescripcion",
	"RUTEntidadMCF",
	"Fecha",
	"SaldoCuentaCambioConversion",
	"SaldoMonedaOriginal",
	"MonedaSaldo",
	"MonedaSaldoDescripcion",
	"Paridad",
--campos de control interno especifico
	"IdCuentaInversion",
	"IdCliente",
	"IdCargoAbonoMovimiento",
	"IdTipoMovimiento",
	"FechaEfectivaMovimiento",
	"Observaciones"

) AS


				
				SELECT 
					DAT."ID",
					DAT."IdDocumento",
					DAT."FuenteDatos",
				    DAT."Estado" ,
				    DAT."EstadoSICAM",
				    DAT."SICAM",
				    COALESCE(a."Descripcion",'') 				AS	"SICAMDescripcion",
					DAT."TipoRegistro",
					COALESCE(c."Descripcion",'') 				AS	"TipoRegistroDescripcion",
					DAT."RUTEntidadMCF",
					DAT."Fecha",
					DAT."SaldoCuentaCambioConversion",
					DAT."SaldoMonedaOriginal",
					DAT."MonedaSaldo",
					COALESCE(b."Descripcion",'') 				AS	"MonedaSaldoDescripcion",
					DAT."Paridad",
					0,
					DAT."IdCliente",
					DAT."IdCargoAbonoMovimiento",
					DAT."IdTipoMovimiento",
					DAT."FechaEfectivaMovimiento",
					COALESCE(VAL."CodigoValidacion", '')		AS 		"Observaciones"
				FROM "BSCL.Normativa.BD::NOR.DatosREPO2P2" DAT
				left join "BSCL.Normativa.BD::DOC.Diccionario" a on a."Tipo"='REPO2P2RESULTADOWS' 	and DAT."SICAM"=a."Codigo" 
				left join "BSCL.Normativa.BD::DOC.Diccionario" b on b."Tipo"='REPO2P2MONEDAISO' 	and DAT."MonedaSaldo"=b."Codigo" 
				left join "BSCL.Normativa.BD::DOC.Diccionario" c on c."Tipo"='REPO2P2TIPOREGISTRO' 	and DAT."TipoRegistro"=c."Codigo"				
				LEFT JOIN (
					SELECT "CodigoValidacion", "IdDocumento" ,"ID"
						FROM (
							SELECT 
								R."CodigoValidacion", DT."IdDocumento", DT."ID", 
								ROW_NUMBER() OVER (
									PARTITION BY DT."ID" 
									ORDER BY R."ID" ASC
								) AS rn
							FROM "BSCL.Normativa.BD::TRI.ResumenValidacion" AS R
							INNER JOIN "BSCL.Normativa.BD::NOR.DatosREPO2P2" AS DT ON DT."IdDocumento" = R."IdDocumento"
							INNER JOIN "BSCL.Normativa.BD::TRI.DetalleValidacion" AS DV ON DV."IdResumenValidacion" = R."ID" AND DV."IdDetalleDocumento" = DT."ID" 
						) AS t WHERE t.rn = 1
				) AS VAL ON  DAT."IdDocumento" = VAL."IdDocumento" AND DAT."ID" = VAL."ID"
				

WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NOR.REPO2P2.Datos" TO bcs_desarrollador WITH GRANT OPTION;