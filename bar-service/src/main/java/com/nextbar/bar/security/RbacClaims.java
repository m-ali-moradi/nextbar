package com.nextbar.bar.security;

import java.util.List;

public record RbacClaims(List<String> roles, List<String> assignments) {
}
