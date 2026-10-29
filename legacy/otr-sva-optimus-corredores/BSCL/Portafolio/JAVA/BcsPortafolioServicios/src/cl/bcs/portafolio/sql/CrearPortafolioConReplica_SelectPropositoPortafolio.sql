SELECT proposito."IdPropositoPortafolio" 
FROM "BSCL.Portafolio.BD::POR.PropositoPortafolio" AS proposito
WHERE proposito."IdPropositoPortafolio" IN (:PropositoPortafolio) AND proposito."IdEstado" IN ('ACT');


