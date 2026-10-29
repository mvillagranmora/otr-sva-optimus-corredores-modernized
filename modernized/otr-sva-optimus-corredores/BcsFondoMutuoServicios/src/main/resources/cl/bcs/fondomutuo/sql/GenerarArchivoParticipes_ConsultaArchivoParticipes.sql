 SELECT DISTINCT
 	Cliente."DocumentoIdentidad"
 	,Cliente."Tipo" AS "TipoPersona"
	,Cliente."NombreCompleto"
	,Cliente."Nombres"
	,Cliente."ApellidoPaterno"
	,Cliente."ApellidoMaterno"
	,Direccion."Calle"
	,Direccion."Numero"
	,Direccion."IdCiudad" AS "CodigoCiudad"
	,Telefono.TEL_NUMERO  AS "Telefono"
	,Cliente."Sexo"
	,Cliente."EstadoCivil"
	,Cliente."RegimenConyugal"
	,Pais."NombrePais" AS "Nacionalidad"
	,Cliente."FechaNacimiento"
	,'01'  AS "Constante1"
	,Cuenta."CodigoBanco" AS "CodigoBanco"
    ,Cuenta."NumeroCuentaBancaria" AS "CuentaCorriente"
	,Indicador."ValorIndicador" AS "ContratoCustodia"
	,'002' AS "Constante2"
	,Cliente."CodigoEmpleadoEjecutivo" AS "CodigoAgente"
	,Direccion."IdDivisionPais" AS "CodigoComuna"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SolicitudAporteRescate
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SerieDeFondo 
	ON (SerieDeFondo."IdSerieFondo" = SolicitudAporteRescate."IdSerieFondo" )
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS Fondo 
	ON (Fondo."IdFondo" = SerieDeFondo."IdFondo" )
LEFT OUTER  JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio 
	ON  (Portafolio."IdPortafolio" = SolicitudAporteRescate."IdPortafolio" )
LEFT OUTER  JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS Cliente 
	ON  (Cliente."IdCliente" = Portafolio."IdCliente" )
LEFT JOIN "BSCL.Plataforma.BD.Vistas::REG.ConsultarPais" AS Pais 
    ON Cliente."IdPaisNacimiento" = Pais."IdPais"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS Direccion
    ON Direccion."IdDireccionFisica" = Cliente."IdDireccionFisicaPrincipal"
LEFT JOIN "BSCL.Homologacion.BD.Vistas::HomologaTelefono" AS Telefono
    ON Telefono.TEL_ID = Cliente."IdTelefonoPrincipal"
LEFT JOIN"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" AS Indicador 
	ON(Cliente."IdCliente" = Indicador."IdCliente" AND Indicador."CodigoTipoIndicador"='CC')
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS TipoMovimientoFondoMutuo 
	ON (TipoMovimientoFondoMutuo."IdTipoMovimientoFondoMutuo" = SolicitudAporteRescate."IdTipoMovimientoFondoMutuo")
LEFT JOIN (SELECT * FROM (
		SELECT 
            Cta."IdCliente",
            Cta."CodigoBanco",
            Cta."NumeroCuentaBancaria",
            ROW_NUMBER() OVER (PARTITION BY Cta."IdCliente" ORDER BY CASE WHEN Cta."Predeterminado" = 'UI_SI' THEN 1 ELSE 2 END) AS RN
        FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentasBancarias" Cta
    ) T
    WHERE T.RN = 1
) Cuenta
    ON Cuenta."IdCliente" = Cliente."IdCliente"
WHERE SolicitudAporteRescate."FechaAceptacion" = :FechaCurse
  AND ( SolicitudAporteRescate."Estado" = :Estado OR ((:Estado = 'TODOS' OR :Estado IS NULL OR :Estado = '') AND SolicitudAporteRescate."Estado" != 'ANU'))
  AND (TipoMovimientoFondoMutuo."IndicadorAporteRescate" = :TipoSolicitud)
  AND ( :FondoMutuo IS NULL OR :FondoMutuo = '' OR SolicitudAporteRescate."IdSerieFondo" = :FondoMutuo )
  AND ( :IdEmisor IS NULL OR :IdEmisor = '' OR Fondo."IdEmisor" = :IdEmisor );