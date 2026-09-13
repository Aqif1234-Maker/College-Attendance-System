package com.attendance.oe;

import com.attendance.common.ApiResponse;
import com.attendance.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/oe/teaching-slots")
public class OeTeachingSlotController {

    private final OeTeachingSlotService oeTeachingSlotService;
    private final CurrentUser currentUser;

    public OeTeachingSlotController(OeTeachingSlotService oeTeachingSlotService, CurrentUser currentUser) {
        this.oeTeachingSlotService = oeTeachingSlotService;
        this.currentUser = currentUser;
    }

    @PostMapping
    @PreAuthorize("hasRole('CLASS_COORDINATOR')")
    public ApiResponse<OeTeachingSlotDto> assign(@Valid @RequestBody AssignOeSlotRequest request) {
        return ApiResponse.ok("Teaching slot assigned", oeTeachingSlotService.assign(request, currentUser.getUserId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CLASS_COORDINATOR')")
    public ApiResponse<OeTeachingSlotDto> reassign(
            @PathVariable Long id, @Valid @RequestBody AssignOeSlotRequest request) {
        return ApiResponse.ok("Teaching slot reassigned", oeTeachingSlotService.reassign(id, request, currentUser.getUserId()));
    }

    @GetMapping
    public ApiResponse<List<OeTeachingSlotDto>> listBySemester(@RequestParam Long semesterId) {
        return ApiResponse.ok(oeTeachingSlotService.listBySemester(semesterId));
    }

    // A teacher checks this to know whether they hold an ungranted slot and should
    // see the "Create My OE Subject" prompt.
    @GetMapping("/my-slots")
    public ApiResponse<List<OeTeachingSlotDto>> mySlots() {
        return ApiResponse.ok(oeTeachingSlotService.listForTeacher(currentUser.getUserId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<OeTeachingSlotDto> get(@PathVariable Long id) {
        return ApiResponse.ok(oeTeachingSlotService.get(id));
    }
}