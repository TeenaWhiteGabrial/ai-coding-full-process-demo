package com.aistudio.service.controller.common;

import com.aistudio.service.common.Result;
import com.aistudio.service.dto.request.DictCreateRequest;
import com.aistudio.service.dto.request.DictUpdateRequest;
import com.aistudio.service.dto.response.DictTreeVO;
import com.aistudio.service.service.DictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Dictionary Management")
@RestController
@RequestMapping("/common/dict")
@RequiredArgsConstructor
public class CommonDictController {

    private final DictService dictService;

    @Operation(summary = "Dictionary tree")
    @GetMapping("/tree")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OP_ADMIN')")
    public Result<List<DictTreeVO>> tree(@RequestParam(required = false) String dictType) {
        return Result.success(dictService.getDictTree(dictType));
    }

    @Operation(summary = "Dictionary detail")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OP_ADMIN')")
    public Result<DictTreeVO> detail(@PathVariable Long id) {
        return Result.success(dictService.getDictById(id));
    }

    @Operation(summary = "Create dictionary item")
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Long> create(@Valid @RequestBody DictCreateRequest request) {
        return Result.success(dictService.createDict(request));
    }

    @Operation(summary = "Update dictionary item")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody DictUpdateRequest request) {
        dictService.updateDict(id, request);
        return Result.success();
    }

    @Operation(summary = "Delete dictionary item")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        dictService.deleteDict(id);
        return Result.success();
    }
}
