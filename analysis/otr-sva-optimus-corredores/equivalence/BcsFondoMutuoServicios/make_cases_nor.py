"""Writes the case inputs of step 4A (FMM-NOR-02..44) under cases/. The goldens are recorded by legacy-oracle/record.sh FMM-NOR (unmodified legacy)."""
import cases_nor_a
import cases_nor_b

if __name__ == "__main__":
    cases_nor_a.generar()
    cases_nor_b.generar()
    cases_nor_b.comision_trimestral()
    print("cases written")
