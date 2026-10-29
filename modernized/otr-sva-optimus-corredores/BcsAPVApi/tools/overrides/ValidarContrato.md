# Override: ValidarContrato (legacy ValidaContratoSAAPV)

File: `src/main/java/cl/bcs/apv/api/dto/validarcontrato/ValidarContratoEntrada.java`, method `porDefecto()`.

The legacy `ValidaContratoSAAPV_Entrada` constructor does `p_ContratosSAAPV = new ArrayList<>(); p_ContratosSAAPV.add(null);`, so a JSON without `p_ContratosSAAPV` gives the list `[null]`
(and `fila.FolioContratoSAAPV` throws NullPointerException, case APV-A-225). The generator emitted an empty list: the default of the component must be `[null]`.
A regeneration must keep this default (generator: initialisers with `add(...)` statements in the constructor are not read).
