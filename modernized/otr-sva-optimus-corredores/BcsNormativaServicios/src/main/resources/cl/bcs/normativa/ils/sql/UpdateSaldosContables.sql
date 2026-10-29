UPSERT "BSCL.Normativa.BD::ILS.p_SaldoCuentaContable"
("IdCodigoContabilidad","MontoDebe","MontoHaber","IdCodigoContabilidadPadre", "Nivel") 
VALUES(:IdCodigoContabilidad,:MontoDebe,:MontoHaber,:IdCodigoContabilidadPadre,:Nivel) 
WHERE "IdCodigoContabilidad"=:IdCodigoContabilidad;