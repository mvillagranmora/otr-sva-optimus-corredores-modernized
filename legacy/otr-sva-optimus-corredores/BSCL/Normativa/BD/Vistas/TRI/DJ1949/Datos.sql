

SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1949.Datos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1949.Datos" (
  		"ID",
    	"IdDocumento",
   		"VboUsuario",
   		"VboUsuarioDescripcion",
    	"Observaciones",
    	"FuenteDatos",
		"IdEventoCapitalCliente",
		"Mercado",
		"Nemo",
		"TipoRegistro",
		"FecMovto",
		"SecEvento",
		"RutSociedad",
		"TipoSociedad",
		"RutCliente",
		"Cuenta",
		"CantCuotas3112",
		"Monto",
		"MontoActualizado",
		"MtoDivReaAfecGCCCIDPC_C6",
		"MtoDivReaAfecGCCCIDPCAL311216_C7",
		"MtoDivReaAfecGCCDIDPCVol_C8",
		"MtoDivReaAfecGCSinDer_C9",
		"MtoDivReaREXTribCumpRentasRap_C10",
		"MtoDivReaREXTribCumpOtrasRentas_C11",
		"MtoDivReaREXTribCumpExcDist_C12",
		"MtoDivReaREXTribCumpUtiISFUT_C13",
		"MtoDivReaREXTribCumpRentasISFUT_C14",
		"MtoDivReaREXRentExIGCAfecIAart11_C15",
		"MtoDivReaREXRentExIGCIA_C16",
		"MtoDivReaREXNoConstRenta_C17",
		"CIGCDesde01012017RentAfecNoRestSDD_C18",
		"CIGCDesde01012017RentAfecNoRestCDD_C19",
		"CIGCDesde01012017RentAfecNoRestSDD_C20",
		"CIGCDesde01012017RentAfecNoRestCDD_C21",
		"CIGCDesde01012017RentAfecRestSDD_C22",
		"CIGCDesde01012017RentAfecRestCDD_C23",
		"CIGCDesde01012017RentExenRestSDD_C24",
		"CIGCDesde01012017RentExenRestCDD_C25",
		"CIGCDesde01012017CredIPE_C26",
		"CIGCHasta31122016RentAfecSDD_C27",
		"CIGCHasta31122016RentAfecCDD_C28",
		"CIGCHasta31122016RentExenSDD_C29",
		"CIGCHasta31122016RentExenCDD_C30",
		"CIGCHasta31122016CredIPE_C31",
		"CIGCImptoTasaAdicional_C32",
		"TasaEfectCredFUT",
		"TasaEfectCredFUNT",
		"DevCapArt17N7LIR_C33",
		"NumCertificado_C34",
		"AcogidoISFUT",
		"OrigenCalificacion"
) AS

SELECT 
		a."ID",
    	a."IdDocumento",
   		a."VboUsuario",
   		COALESCE(b."Descripcion",'') 				AS	"VboUsuarioDescripcion",
    	COALESCE(VAL."CodigoValidacion" ,a."Observaciones") AS "Observaciones",
    	a."FuenteDatos",
		a."IdEventoCapitalCliente",
		a."Mercado",
		a."Nemo",
		a."TipoRegistro",
		a."FecMovto",
		a."SecEvento",
		a."RutSociedad",
		a."TipoSociedad",
		a."RutCliente",
		a."Cuenta",
		a."CantCuotas3112",
		a."Monto",
		a."MontoActualizado",
		ROUND(a."MtoDivReaAfecGCCCIDPC_C6"),
		ROUND(a."MtoDivReaAfecGCCCIDPCAL311216_C7"),
		ROUND(a."MtoDivReaAfecGCCDIDPCVol_C8"),
		ROUND(a."MtoDivReaAfecGCSinDer_C9"),
		ROUND(a."MtoDivReaREXTribCumpRentasRap_C10"),
		ROUND(a."MtoDivReaREXTribCumpOtrasRentas_C11"),
		ROUND(a."MtoDivReaREXTribCumpExcDist_C12"),
		ROUND(a."MtoDivReaREXTribCumpUtiISFUT_C13"),
		ROUND(a."MtoDivReaREXTribCumpRentasISFUT_C14"),
		ROUND(a."MtoDivReaREXRentExIGCAfecIAart11_C15"),
		ROUND(a."MtoDivReaREXRentExIGCIA_C16"),
		ROUND(a."MtoDivReaREXNoConstRenta_C17"),
		ROUND(a."CIGCDesde01012017RentAfecNoRestSDD_C18"),
		ROUND(a."CIGCDesde01012017RentAfecNoRestCDD_C19"),
		ROUND(a."CIGCDesde01012017RentAfecNoRestSDD_C20"),
		ROUND(a."CIGCDesde01012017RentAfecNoRestCDD_C21"),
		ROUND(a."CIGCDesde01012017RentAfecRestSDD_C22"),
		ROUND(a."CIGCDesde01012017RentAfecRestCDD_C23"),
		ROUND(a."CIGCDesde01012017RentExenRestSDD_C24"),
		ROUND(a."CIGCDesde01012017RentExenRestCDD_C25"),
		ROUND(a."CIGCDesde01012017CredIPE_C26"),
		ROUND(a."CIGCHasta31122016RentAfecSDD_C27"),
		ROUND(a."CIGCHasta31122016RentAfecCDD_C28"),
		ROUND(a."CIGCHasta31122016RentExenSDD_C29"),
		ROUND(a."CIGCHasta31122016RentExenCDD_C30"),
		ROUND(a."CIGCHasta31122016CredIPE_C31"),
		ROUND(a."CIGCImptoTasaAdicional_C32"),
		a."TasaEfectCredFUT",
		a."TasaEfectCredFUNT",
		ROUND(a."DevCapArt17N7LIR_C33"),
		a."NumCertificado_C34",
		a."AcogidoISFUT",
		a."OrigenCalificacion"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1949" a
left join "BSCL.Normativa.BD::DOC.Diccionario" b
    on b."Tipo"='VistoBueno' and a."VboUsuario"=b."Codigo"
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
			    INNER JOIN "BSCL.Normativa.BD::TRI.DatosDJ1949" AS DT ON DT."IdDocumento" = R."IdDocumento"
			    INNER JOIN "BSCL.Normativa.BD::TRI.DetalleValidacion" AS DV ON DV."IdResumenValidacion" = R."ID" AND DV."IdDetalleDocumento" = DT."ID" 
			) AS t WHERE t.rn = 1
        ) AS VAL ON  a."IdDocumento" = VAL."IdDocumento" AND a."ID" = VAL."ID"
WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1949.Datos" TO bcs_desarrollador WITH GRANT OPTION;
