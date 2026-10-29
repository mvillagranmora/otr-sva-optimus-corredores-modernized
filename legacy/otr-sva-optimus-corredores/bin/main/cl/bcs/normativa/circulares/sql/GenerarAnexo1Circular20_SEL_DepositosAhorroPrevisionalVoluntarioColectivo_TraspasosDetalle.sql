SELECT 
    "IdAccion",
    "Accion",
    --Afp
    to_bigint("Afp_NumeroTraspasos") AS "Afp_NumeroTraspasos",
    to_decimal("Afp_Miles",26,8) AS "Afp_Miles",
    --BancosInstitucionesFinancieras
    to_bigint("BancosInstitucionesFinancieras_NumeroTraspasos") AS "BancosInstitucionesFinancieras_NumeroTraspasos",
    to_decimal("BancosInstitucionesFinancieras_Miles",26,8) AS "BancosInstitucionesFinancieras_Miles",
    --CorredoresBolsa
    to_bigint("CorredoresBolsa_NumeroTraspasos") AS "CorredoresBolsa_NumeroTraspasos",
    to_decimal("CorredoresBolsa_Miles",26,8) AS "CorredoresBolsa_Miles",
    --AgentesValores
    to_bigint("AgentesValores_NumeroTraspasos") AS "AgentesValores_NumeroTraspasos",
    to_decimal("AgentesValores_Miles",26,8) AS "AgentesValores_Miles",
    --FondosMutuos
    to_bigint("FondosMutuos_NumeroTraspasos") AS "FondosMutuos_NumeroTraspasos",
    to_decimal("FondosMutuos_Miles",26,8) AS "FondosMutuos_Miles",
    --FondosInversion
    to_bigint("FondosInversion_NumeroTraspasos") AS "FondosInversion_NumeroTraspasos",
    to_decimal("FondosInversion_Miles",26,8) AS "FondosInversion_Miles",
    --CompaniasSeguros
    to_bigint("CompaniasSeguros_NumeroTraspasos") AS "CompaniasSeguros_NumeroTraspasos",
    to_decimal("CompaniasSeguros_Miles",26,8) AS "CompaniasSeguros_Miles",
    --FondosVivienda
    to_bigint("FondosVivienda_NumeroTraspasos") AS "FondosVivienda_NumeroTraspasos",
    to_decimal("FondosVivienda_Miles",26,8) AS "FondosVivienda_Miles",
    --OtrasEntidadesAutorizadasSvs
    to_bigint("OtrasEntidadesAutorizadasSvs_NumeroTraspasos") AS "OtrasEntidadesAutorizadasSvs_NumeroTraspasos",
    to_decimal("OtrasEntidadesAutorizadasSvs_Miles",26,8) AS "OtrasEntidadesAutorizadasSvs_Miles"
from 
(
    SELECT
        1 AS "IdAccion",
        'Realizados' AS "Accion",
        --Afp
        1 AS "Afp_NumeroTraspasos",
        2 AS "Afp_Miles",
        --BancosInstitucionesFinancieras
        3 AS "BancosInstitucionesFinancieras_NumeroTraspasos",
        4 AS "BancosInstitucionesFinancieras_Miles",
        --CorredoresBolsa
        5 AS "CorredoresBolsa_NumeroTraspasos",
        6 AS "CorredoresBolsa_Miles",
        --AgentesValores
        7 AS "AgentesValores_NumeroTraspasos",
        8 AS "AgentesValores_Miles",
        --FondosMutuos
        9 AS "FondosMutuos_NumeroTraspasos",
        10 AS "FondosMutuos_Miles",
        --FondosInversion
        11 AS "FondosInversion_NumeroTraspasos",
        12 AS "FondosInversion_Miles",
        --CompaniasSeguros
        13 AS "CompaniasSeguros_NumeroTraspasos",
        14 AS "CompaniasSeguros_Miles",
        --FondosVivienda
        15 AS "FondosVivienda_NumeroTraspasos",
        16 AS "FondosVivienda_Miles",
        --OtrasEntidadesAutorizadasSvs
        17 AS "OtrasEntidadesAutorizadasSvs_NumeroTraspasos",
        18 AS "OtrasEntidadesAutorizadasSvs_Miles"
    FROM dummy UNION all
    SELECT
        2 AS "IdAccion",
        'Recibidos' AS "Accion",
        --Afp
        19 AS "Afp_NumeroTraspasos",
        20 AS "Afp_Miles",
        --BancosInstitucionesFinancieras
        21 AS "BancosInstitucionesFinancieras_NumeroTraspasos",
        22 AS "BancosInstitucionesFinancieras_Miles",
        --CorredoresBolsa
        23 AS "CorredoresBolsa_NumeroTraspasos",
        24 AS "CorredoresBolsa_Miles",
        --AgentesValores
        25 AS "AgentesValores_NumeroTraspasos",
        26 AS "AgentesValores_Miles",
        --FondosMutuos
        27 AS "FondosMutuos_NumeroTraspasos",
        28 AS "FondosMutuos_Miles",
        --FondosInversion
        29 AS "FondosInversion_NumeroTraspasos",
        30 AS "FondosInversion_Miles",
        --CompaniasSeguros
        31 AS "CompaniasSeguros_NumeroTraspasos",
        32 AS "CompaniasSeguros_Miles",
        --FondosVivienda
        33 AS "FondosVivienda_NumeroTraspasos",
        34 AS "FondosVivienda_Miles",
        --OtrasEntidadesAutorizadasSvs
        35 AS "OtrasEntidadesAutorizadasSvs_NumeroTraspasos",
        36 AS "OtrasEntidadesAutorizadasSvs_Miles"
    FROM DUMMY)
WHERE 1=2 --Se deja la estructura dummy, ya que actualmente NO se maneja apv colectivo, pero le java si esta capacitado para leer esta informacion
ORDER BY "IdAccion";