package com.pbo.aritmatika;

import com.pbo.aritmatika.Aritmatika;
import com.pbo.aritmatika.AritmatikaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Set;

@Controller
public class AritmatikaController {

    private static final Set<String> OPERASI_VALID = Set.of("+", "-", "x", "/");

    private final AritmatikaRepository repository;

    public AritmatikaController(AritmatikaRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/")
    public String index(
            @RequestParam(name = "edit", required = false) Integer editId,
            Model model) {

        // Nilai flash dari proses hitung dipertahankan agar form tidak kembali kosong.
        if (!model.containsAttribute("aritmatika")) {
            model.addAttribute("aritmatika", new Aritmatika());
        }

        // Data edit hanya digunakan pada baris riwayat yang sedang dipilih.
        if (!model.containsAttribute("editData")) {
            Aritmatika editData = editId == null
                    ? new Aritmatika()
                    : repository.findById(editId).orElse(null);

            if (editId != null && editData == null) {
                model.addAttribute("pesanError", "Riwayat yang akan diedit tidak ditemukan.");
                editId = null;
                editData = new Aritmatika();
            }

            if (editData != null) {
                editData.setHasil(hitung(editData.getBil1(), editData.getOperasi(), editData.getBil2()));
            }
            model.addAttribute("editData", editData);
        }

        model.addAttribute("editId", editId);
        isiDataHalaman(model);
        return "aritmatika";
    }

    // Form utama selalu membuat riwayat baru, bukan mengedit baris yang sudah ada.
    @PostMapping("/simpan")
    public String simpan(
            @ModelAttribute("aritmatika") Aritmatika aritmatika,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        validasiInput(aritmatika, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("editData", new Aritmatika());
            model.addAttribute("editId", null);
            isiDataHalaman(model);
            return "aritmatika";
        }

        // Salin hanya kolom database agar form utama tidak membawa ID lama.
        Aritmatika dataBaru = new Aritmatika();
        dataBaru.setBil1(aritmatika.getBil1());
        dataBaru.setOperasi(aritmatika.getOperasi());
        dataBaru.setBil2(aritmatika.getBil2());

        Aritmatika tersimpan = repository.save(dataBaru);
        String hasil = hitung(tersimpan.getBil1(), tersimpan.getOperasi(), tersimpan.getBil2());

        // Form utama dikembalikan dengan nilai input terakhir, tetapi tanpa ID.
        Aritmatika formTerakhir = new Aritmatika();
        formTerakhir.setBil1(aritmatika.getBil1());
        formTerakhir.setOperasi(aritmatika.getOperasi());
        formTerakhir.setBil2(aritmatika.getBil2());

        redirectAttributes.addFlashAttribute("aritmatika", formTerakhir);
        redirectAttributes.addFlashAttribute("hasilTerakhir", hasil);
        redirectAttributes.addFlashAttribute("pesan", "Operasi berhasil disimpan.");

        return "redirect:/";
    }

    // Memperbarui data langsung dari input pada baris riwayat yang dipilih.
    @PostMapping("/edit/{id}")
    public String updateRiwayat(
            @PathVariable Integer id,
            @ModelAttribute("editData") Aritmatika editData,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        validasiInput(editData, bindingResult);

        Aritmatika dataDatabase = repository.findById(id).orElse(null);
        if (dataDatabase == null) {
            redirectAttributes.addFlashAttribute("pesanError", "Riwayat yang akan diedit tidak ditemukan.");
            return "redirect:/";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("aritmatika", new Aritmatika());
            model.addAttribute("editId", id);
            model.addAttribute("editError", pesanValidasi(bindingResult));
            isiDataHalaman(model);
            return "aritmatika";
        }

        dataDatabase.setBil1(editData.getBil1());
        dataDatabase.setOperasi(editData.getOperasi());
        dataDatabase.setBil2(editData.getBil2());
        repository.save(dataDatabase);

        redirectAttributes.addFlashAttribute("pesan", "Riwayat berhasil diperbarui.");
        return "redirect:/";
    }

    @PostMapping("/hapus/{id}")
    public String hapus(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            redirectAttributes.addFlashAttribute("pesan", "Riwayat berhasil dihapus.");
        } else {
            redirectAttributes.addFlashAttribute("pesanError", "Riwayat tidak ditemukan.");
        }

        return "redirect:/";
    }

    private void validasiInput(Aritmatika aritmatika, BindingResult bindingResult) {
        // Tipe Integer menolak masukan selain bilangan bulat.
        if (aritmatika.getBil1() == null && !bindingResult.hasFieldErrors("bil1")) {
            bindingResult.rejectValue("bil1", "required", "Bilangan 1 wajib diisi dengan integer.");
        }

        if (aritmatika.getBil2() == null && !bindingResult.hasFieldErrors("bil2")) {
            bindingResult.rejectValue("bil2", "required", "Bilangan 2 wajib diisi dengan integer.");
        }

        if (aritmatika.getOperasi() == null || !OPERASI_VALID.contains(aritmatika.getOperasi())) {
            if (!bindingResult.hasFieldErrors("operasi")) {
                bindingResult.rejectValue("operasi", "invalid", "Pilih operator yang tersedia.");
            }
        }
    }

    private String pesanValidasi(BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors("bil1")) {
            return "Bilangan 1 harus berupa integer yang valid.";
        }
        if (bindingResult.hasFieldErrors("bil2")) {
            return "Bilangan 2 harus berupa integer yang valid.";
        }
        if (bindingResult.hasFieldErrors("operasi")) {
            return "Pilih operator yang tersedia.";
        }
        return "Periksa kembali input pada baris riwayat.";
    }

    private void isiDataHalaman(Model model) {
        model.addAttribute("operators", List.of("+", "-", "x", "/"));

        List<Aritmatika> riwayat = repository.findAll(
                Sort.by(Sort.Direction.DESC, "idAritmatika")
        );

        // Hasil dihitung ulang untuk tampilan dan tidak disimpan ke database.
        riwayat.forEach(item -> item.setHasil(
                hitung(item.getBil1(), item.getOperasi(), item.getBil2())
        ));

        model.addAttribute("riwayat", riwayat);
    }

    private String hitung(Integer bil1, String operasi, Integer bil2) {
        if (bil1 == null || bil2 == null || operasi == null) {
            return "-";
        }

        long angka1 = bil1.longValue();
        long angka2 = bil2.longValue();

        return switch (operasi) {
            case "+" -> String.valueOf(angka1 + angka2);
            case "-" -> String.valueOf(angka1 - angka2);
            case "x" -> String.valueOf(angka1 * angka2);
            case "/" -> {
                if (angka2 == 0) {
                    yield "Tidak terdefinisi (dibagi 0)";
                }
                yield BigDecimal.valueOf(angka1)
                        .divide(BigDecimal.valueOf(angka2), MathContext.DECIMAL128)
                        .stripTrailingZeros()
                        .toPlainString();
            }
            default -> "-";
        };
    }
}
