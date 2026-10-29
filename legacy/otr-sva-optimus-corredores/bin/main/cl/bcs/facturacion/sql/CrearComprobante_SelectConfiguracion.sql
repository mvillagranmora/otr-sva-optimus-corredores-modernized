Select "IdEmpresa",
 MAX(Case When "CodigoParametro"='SEPARAPORAGENTE' 		Then "Valor" Else Null End) As "SeparaPorEjecutivo",
 MAX(Case When "CodigoParametro"='SEPARAPORBOLSA' 		Then "Valor" Else Null End) As "SeparaPorBolsa",
 MAX(Case When "CodigoParametro"='SEPARAPORSUCURSAL'	Then "Valor" Else Null End) As "SeparaPorSucursal"
FROM "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion"
WHERE "IdEmpresa" = :IdEmpresa
GROUP BY "IdEmpresa";
						      

