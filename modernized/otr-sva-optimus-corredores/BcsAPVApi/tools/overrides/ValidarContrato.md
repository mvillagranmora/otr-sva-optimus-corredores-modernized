# Override: ValidarContrato (legacy ValidaContratoSAAPV)

File: `src/main/java/cl/bcs/apv/api/dto/validarcontrato/ValidarContratoEntrada.java`, method `porDefecto()`.

The legacy `ValidaContratoSAAPV_Entrada` constructor does `p_ContratosSAAPV = new ArrayList<>(); p_ContratosSAAPV.add(null);`, so a JSON without `p_ContratosSAAPV` gives the list `[null]`
(and `fila.FolioContratoSAAPV` throws NullPointerException, case APV-A-225). The generator emitted an empty list: the default of the component must be `[null]`.
A regeneration must keep this default (generator: initialisers with `add(...)` statements in the constructor are not read).

Since 2026-10-30 the generator APPLIES this override (`modelo.cargar_overrides()` reads the `OVERRIDE-DEFAULT:` lines of the files in this folder; `generar.py` emits
the `[null]` default and `generar_contrato.py` writes `[null]` in `CONTRATO_CAMPOS.md`). A line whose class/field is not found in the legacy model stops the generator.
Format: `OVERRIDE-DEFAULT: <Outer.Inner legacy class> <legacy field> = [null]`

OVERRIDE-DEFAULT: ValidaContratoSAAPV.ValidaContratoSAAPV_Entrada p_ContratosSAAPV = [null]
