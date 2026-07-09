/**
 * AUTO-GENERATED — DO NOT EDIT BY HAND.
 * TypeScript types for the Protrack HTTP contract, generated from openapi.json.
 * Regenerate with `npm run generate`; `npm run check` fails CI if this file drifts.
 */
export interface paths {
    "/internal/v1/ai-jobs/{jobId}/progress": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["progress"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/projects": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["list"];
        put?: never;
        post: operations["create"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/projects/{projectId}/signoff": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["signOff"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/projects/{projectId}/preflight": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["latest"];
        put?: never;
        post: operations["start"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/projects/{projectId}/pdf": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["uploadPdf"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/projects/{projectId}/package": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["get"];
        put?: never;
        post: operations["assemble"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/projects/{projectId}/package/items": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["addItem"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/projects/{projectId}/manuscript": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["uploadManuscript"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/projects/{projectId}/documents": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["list_1"];
        put?: never;
        post: operations["create_1"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/projects/{projectId}/comments": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["list_2"];
        put?: never;
        post: operations["add"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/projects/{projectId}/assistant/messages": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["ask"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/projects/{projectId}/analysis": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["latest_1"];
        put?: never;
        post: operations["start_1"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/projects/{id}/transitions": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["transition"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/projects/{id}/members": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["assignMembers"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/notifications:read-all": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["markAllRead"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/notifications/{id}:read": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["markRead"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/issues:bulk-decision": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["bulkDecide"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/issues/{issueId}/decision": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["decide"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/documents/{documentId}/versions": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["versions"];
        put?: never;
        post: operations["addVersion"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/documents/{documentId}/versions/{versionId}:setCurrent": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["setCurrent"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/auth/refresh": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["refresh"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/auth/logout": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["logout"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/auth/login": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["login"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/users": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["listUsers"];
        put?: never;
        post: operations["create_2"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/users:bulk": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["bulk"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/users/{id}/roles": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["assignRole"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/projects/{id}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["get_1"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch: operations["update"];
        trace?: never;
    };
    "/api/v1/notification-preferences": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["get_2"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch: operations["update_1"];
        trace?: never;
    };
    "/api/v1/comments/{id}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post?: never;
        delete: operations["delete"];
        options?: never;
        head?: never;
        patch: operations["edit"];
        trace?: never;
    };
    "/api/v1/admin/users/{id}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["get_3"];
        put?: never;
        post?: never;
        delete: operations["deactivate"];
        options?: never;
        head?: never;
        patch: operations["update_2"];
        trace?: never;
    };
    "/api/v1/workflow-stages": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["stages"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/reports/workload-by-imprint": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["workloadByImprint"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/reports/throughput": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["throughput"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/reports/overview": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["overview"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/projects/{projectId}/signoffs": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["signoffs"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/projects/{projectId}/package/download": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["download"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/projects/{projectId}/issues": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["issues"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/projects/{projectId}/events": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["stream"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/projects/{projectId}/assistant/thread": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["thread"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/projects/{projectId}/approvals": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["approvals"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/projects/{id}/timeline": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["timeline"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/projects/{id}/activity": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["activity"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/notifications": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["list_3"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/notifications/unread-count": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["unreadCount"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/imprints": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["list_4"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/health": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["health"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/file-versions/{versionId}/download": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["download_1"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/documents/{documentId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["get_4"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/dashboard": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["get_5"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/auth/me": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["me"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/audit-events": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["list_5"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/audit-events:export": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["export"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/ai-jobs/{jobId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["get_6"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/roles": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["roles"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/permissions": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["permissions"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/projects/{projectId}/package/items/{itemId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post?: never;
        delete: operations["removeItem"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/admin/users/{id}/roles/{roleId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post?: never;
        delete: operations["revokeRole"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
}
export type webhooks = Record<string, never>;
export interface components {
    schemas: {
        ProgressUpdateRequest: {
            /** Format: int32 */
            progressPct?: number;
            status?: string;
            partial?: {
                [key: string]: Record<string, never>;
            };
        };
        CreateProjectRequest: {
            title: string;
            isbn?: string;
            /** Format: uuid */
            imprintId: string;
            /** @enum {string} */
            publicationType: "STEM_TEXTBOOK" | "MONOGRAPH" | "JOURNAL" | "REFERENCE";
            discipline?: string;
            brief?: string;
            /** Format: int32 */
            pageExtent?: number;
            trimSize?: string;
            /** @enum {string} */
            priority?: "LOW" | "MEDIUM" | "HIGH";
            /** Format: date */
            dueDate?: string;
            memberUserIds?: string[];
        };
        ImprintResponse: {
            id?: string;
            name?: string;
            code?: string;
        };
        OwnerResponse: {
            id?: string;
            fullName?: string;
            email?: string;
        };
        ProjectMemberResponse: {
            userId?: string;
            fullName?: string;
            email?: string;
            avatarInitials?: string;
            roleInProject?: string;
            owner?: boolean;
            matchScore?: number;
        };
        ProjectResponse: {
            id?: string;
            title?: string;
            isbn?: string;
            publicationType?: string;
            discipline?: string;
            brief?: string;
            /** Format: int32 */
            pageExtent?: number;
            trimSize?: string;
            priority?: string;
            currentStage?: string;
            status?: string;
            /** Format: date */
            dueDate?: string;
            /** Format: date */
            createdDate?: string;
            /** Format: date-time */
            createdAt?: string;
            /** Format: date-time */
            updatedAt?: string;
            imprint?: components["schemas"]["ImprintResponse"];
            owner?: components["schemas"]["OwnerResponse"];
            members?: components["schemas"]["ProjectMemberResponse"][];
        };
        SignoffRequest: {
            decision: string;
            /** Format: int32 */
            qualityScore?: number;
            signature: string;
            notes?: string;
        };
        SignoffResponse: {
            id?: string;
            projectId?: string;
            preflightRunId?: string;
            decision?: string;
            /** Format: int32 */
            qualityScore?: number;
            signatureHash?: string;
            notes?: string;
            signedBy?: string;
            signedByName?: string;
            stage?: string;
            /** Format: date-time */
            createdAt?: string;
        };
        AiJobResponse: {
            jobId?: string;
            projectId?: string;
            jobType?: string;
            status?: string;
            /** Format: int32 */
            progressPct?: number;
            provider?: string;
            model?: string;
            errorMessage?: string;
            /** Format: date-time */
            createdAt?: string;
            /** Format: date-time */
            finishedAt?: string;
        };
        ProductionPdfResponse: {
            documentId?: string;
            versionId?: string;
            fileName?: string;
            stage?: string;
        };
        PackageItemResponse: {
            id?: string;
            documentId?: string;
            itemType?: string;
            label?: string;
            /** Format: int64 */
            sizeBytes?: number;
            /** Format: int32 */
            sortOrder?: number;
        };
        PackageResponse: {
            id?: string;
            projectId?: string;
            status?: string;
            /** Format: int64 */
            totalSizeBytes?: number;
            /** Format: int32 */
            itemCount?: number;
            /** Format: int32 */
            downloadCount?: number;
            /** Format: date-time */
            assembledAt?: string;
            assembledById?: string;
            assembledByName?: string;
            /** Format: date-time */
            createdAt?: string;
            /** Format: date-time */
            updatedAt?: string;
            items?: components["schemas"]["PackageItemResponse"][];
        };
        AddPackageItemRequest: {
            /** Format: uuid */
            documentId: string;
            itemType?: string;
            label?: string;
            /** Format: int32 */
            sortOrder?: number;
        };
        DocumentResponse: {
            id?: string;
            projectId?: string;
            docType?: string;
            title?: string;
            status?: string;
            currentVersion?: components["schemas"]["FileVersionResponse"];
            /** Format: date-time */
            createdAt?: string;
            /** Format: date-time */
            updatedAt?: string;
        };
        FileVersionResponse: {
            id?: string;
            /** Format: int32 */
            versionNo?: number;
            fileName?: string;
            mimeType?: string;
            /** Format: int64 */
            sizeBytes?: number;
            checksumSha256?: string;
            current?: boolean;
            uploadedById?: string;
            uploadedByName?: string;
            /** Format: date-time */
            createdAt?: string;
        };
        CreateCommentRequest: {
            body: string;
            /** Format: uuid */
            parentId?: string;
            contextType?: string;
            /** Format: uuid */
            contextId?: string;
        };
        CommentResponse: {
            id?: string;
            projectId?: string;
            parentId?: string;
            authorId?: string;
            authorName?: string;
            authorInitials?: string;
            authorColor?: string;
            contextType?: string;
            contextId?: string;
            body?: string;
            edited?: boolean;
            /** Format: date-time */
            createdAt?: string;
            /** Format: date-time */
            updatedAt?: string;
        };
        AssistantMessageRequest: {
            content: string;
        };
        AssistantMessageResponse: {
            id?: string;
            role?: string;
            content?: string;
            /** Format: int32 */
            tokens?: number;
            citations?: string[];
            /** Format: date-time */
            createdAt?: string;
        };
        TransitionRequest: {
            toStage: string;
            note?: string;
        };
        TransitionResponse: {
            projectId?: string;
            fromStage?: string;
            toStage?: string;
            triggeredRole?: string;
            approvalGate?: boolean;
            /** Format: date-time */
            occurredAt?: string;
        };
        AssignMembersRequest: {
            members: components["schemas"]["MemberAssignment"][];
        };
        MemberAssignment: {
            /** Format: uuid */
            userId: string;
            roleInProject?: string;
            matchScore?: number;
        };
        BulkDecisionRequest: {
            issueIds: string[];
            decision: string;
        };
        BulkDecisionResponse: {
            /** Format: int32 */
            decided?: number;
            decision?: string;
            issueStatus?: string;
            issueIds?: string[];
        };
        IssueDecisionRequest: {
            decision: string;
            comment?: string;
        };
        IssueDecisionResponse: {
            id?: string;
            issueId?: string;
            decision?: string;
            comment?: string;
            decidedBy?: string;
            decidedByName?: string;
            issueStatus?: string;
            /** Format: date-time */
            createdAt?: string;
        };
        RefreshTokenRequest: {
            refreshToken: string;
        };
        TokenResponse: {
            accessToken?: string;
            refreshToken?: string;
            tokenType?: string;
            /** Format: int64 */
            expiresIn?: number;
            user?: components["schemas"]["UserSummary"];
        };
        UserSummary: {
            id?: string;
            email?: string;
            fullName?: string;
            avatarInitials?: string;
            avatarColor?: string;
            roles?: string[];
            permissions?: string[];
        };
        LoginRequest: {
            email: string;
            password: string;
        };
        CreateUserRequest: {
            email: string;
            fullName: string;
            /** Format: int32 */
            roleId: number;
            password: string;
            avatarColor?: string;
        };
        AdminUserResponse: {
            id?: string;
            email?: string;
            fullName?: string;
            avatarInitials?: string;
            avatarColor?: string;
            status?: string;
            roles?: string[];
            /** Format: date-time */
            lastLoginAt?: string;
        };
        BulkUserRequest: {
            action: string;
            userIds: string[];
        };
        BulkUserResult: {
            /** Format: int32 */
            updated?: number;
            /** Format: int32 */
            skipped?: number;
        };
        AssignRoleRequest: {
            /** Format: int32 */
            roleId: number;
        };
        UpdateProjectRequest: {
            title?: string;
            isbn?: string;
            /** Format: uuid */
            imprintId?: string;
            /** @enum {string} */
            publicationType?: "STEM_TEXTBOOK" | "MONOGRAPH" | "JOURNAL" | "REFERENCE";
            discipline?: string;
            brief?: string;
            /** Format: int32 */
            pageExtent?: number;
            trimSize?: string;
            /** @enum {string} */
            priority?: "LOW" | "MEDIUM" | "HIGH";
            /** @enum {string} */
            status?: "ACTIVE" | "ON_HOLD" | "COMPLETED" | "ARCHIVED";
            /** Format: date */
            dueDate?: string;
        };
        Item: {
            type: string;
            inAppEnabled: boolean;
            emailEnabled: boolean;
        };
        UpdatePreferencesRequest: {
            preferences: components["schemas"]["Item"][];
        };
        NotificationPreferenceResponse: {
            type?: string;
            label?: string;
            inAppEnabled?: boolean;
            emailEnabled?: boolean;
        };
        UpdateCommentRequest: {
            body: string;
        };
        UpdateUserRequest: {
            fullName?: string;
            avatarColor?: string;
            status?: string;
        };
        WorkflowStageResponse: {
            code?: string;
            name?: string;
            /** Format: int32 */
            sequence?: number;
            description?: string;
        };
        ImprintWorkloadResponse: {
            /** Format: int64 */
            totalActive?: number;
            items?: components["schemas"]["Item"][];
        };
        Point: {
            month?: string;
            /** Format: int64 */
            completed?: number;
        };
        ThroughputResponse: {
            range?: string;
            points?: components["schemas"]["Point"][];
        };
        ReportOverviewResponse: {
            range?: string;
            /** Format: date */
            periodStart?: string;
            /** Format: date */
            periodEnd?: string;
            /** Format: double */
            turnaroundDays?: number;
            /** Format: double */
            onTimePercentage?: number;
            /** Format: double */
            avgAiConfidence?: number;
            /** Format: double */
            qaPassPercentage?: number;
            /** Format: int64 */
            completedProjects?: number;
            /** Format: int64 */
            qaSignoffs?: number;
        };
        Pageable: {
            /** Format: int32 */
            page?: number;
            /** Format: int32 */
            size?: number;
            sort?: string[];
        };
        PageResponseProjectSummaryResponse: {
            content?: components["schemas"]["ProjectSummaryResponse"][];
            /** Format: int32 */
            page?: number;
            /** Format: int32 */
            size?: number;
            /** Format: int64 */
            totalElements?: number;
            /** Format: int32 */
            totalPages?: number;
            sort?: string;
            hasNext?: boolean;
        };
        ProjectSummaryResponse: {
            id?: string;
            title?: string;
            isbn?: string;
            publicationType?: string;
            discipline?: string;
            imprintName?: string;
            currentStage?: string;
            status?: string;
            priority?: string;
            /** Format: date */
            dueDate?: string;
            ownerName?: string;
        };
        CheckView: {
            key?: string;
            result?: string;
            detail?: string;
        };
        IssueView: {
            id?: string;
            category?: string;
            severity?: string;
            title?: string;
            recommendation?: string;
            pageRef?: string;
            source?: string;
            status?: string;
            /** Format: date-time */
            createdAt?: string;
        };
        PreflightDetailResponse: {
            id?: string;
            projectId?: string;
            aiJobId?: string;
            pdfVersionId?: string;
            standard?: string;
            /** Format: int32 */
            overallScore?: number;
            passed?: boolean;
            /** Format: int32 */
            totalIssues?: number;
            /** Format: int32 */
            highSeverity?: number;
            status?: string;
            /** Format: date-time */
            ranAt?: string;
            /** Format: date-time */
            createdAt?: string;
            checks?: components["schemas"]["CheckView"][];
            issues?: components["schemas"]["IssueView"][];
        };
        StreamingResponseBody: Record<string, never>;
        IssueResponse: {
            id?: string;
            projectId?: string;
            preflightRunId?: string;
            category?: string;
            severity?: string;
            title?: string;
            recommendation?: string;
            pageRef?: string;
            source?: string;
            status?: string;
            /** Format: date-time */
            createdAt?: string;
        };
        SseEmitter: {
            /** Format: int64 */
            timeout?: number;
        };
        DocumentSummaryResponse: {
            id?: string;
            docType?: string;
            title?: string;
            status?: string;
            /** Format: int32 */
            versionCount?: number;
            currentVersion?: components["schemas"]["FileVersionResponse"];
            /** Format: date-time */
            createdAt?: string;
            /** Format: date-time */
            updatedAt?: string;
        };
        PageResponseCommentResponse: {
            content?: components["schemas"]["CommentResponse"][];
            /** Format: int32 */
            page?: number;
            /** Format: int32 */
            size?: number;
            /** Format: int64 */
            totalElements?: number;
            /** Format: int32 */
            totalPages?: number;
            sort?: string;
            hasNext?: boolean;
        };
        AssistantThreadResponse: {
            threadId?: string;
            messages?: components["schemas"]["AssistantMessageResponse"][];
        };
        ApprovalResponse: {
            id?: string;
            projectId?: string;
            stageCode?: string;
            approvalType?: string;
            decision?: string;
            decidedRole?: string;
            decidedBy?: string;
            decidedByName?: string;
            comment?: string;
            /** Format: date-time */
            createdAt?: string;
        };
        AnalysisDetailResponse: {
            id?: string;
            projectId?: string;
            aiJobId?: string;
            /** Format: int32 */
            overallConfidence?: number;
            summary?: string;
            language?: string;
            /** Format: int32 */
            complexityScore?: number;
            complexityLabel?: string;
            /** Format: int32 */
            estimatedWorkingDays?: number;
            metrics?: components["schemas"]["MetricView"][];
            composition?: components["schemas"]["CompositionView"][];
            headings?: components["schemas"]["HeadingView"][];
            risks?: components["schemas"]["RiskView"][];
            suggestedTeam?: components["schemas"]["TeamView"][];
            /** Format: date-time */
            createdAt?: string;
        };
        CompositionView: {
            segment?: string;
            /** Format: double */
            percentage?: number;
        };
        HeadingView: {
            level?: string;
            /** Format: int32 */
            count?: number;
        };
        MetricView: {
            key?: string;
            /** Format: int64 */
            value?: number;
            /** Format: int32 */
            confidence?: number;
        };
        RiskView: {
            severity?: string;
            title?: string;
            description?: string;
        };
        TeamView: {
            userId?: string;
            role?: string;
            /** Format: int32 */
            matchScore?: number;
            rationale?: string;
        };
        TimelineEntryResponse: {
            fromStage?: string;
            toStage?: string;
            triggeredRole?: string;
            triggeredByName?: string;
            note?: string;
            /** Format: date-time */
            occurredAt?: string;
        };
        AuditEventResponse: {
            id?: string;
            eventType?: string;
            entityType?: string;
            entityId?: string;
            summary?: string;
            actorId?: string;
            actorName?: string;
            actorType?: string;
            projectId?: string;
            metadata?: Record<string, never>;
            correlationId?: string;
            /** Format: date-time */
            createdAt?: string;
        };
        NotificationResponse: {
            id?: string;
            type?: string;
            title?: string;
            body?: string;
            projectId?: string;
            relatedEntityType?: string;
            relatedEntityId?: string;
            read?: boolean;
            /** Format: date-time */
            readAt?: string;
            /** Format: date-time */
            sentAt?: string;
            /** Format: date-time */
            createdAt?: string;
        };
        PageResponseNotificationResponse: {
            content?: components["schemas"]["NotificationResponse"][];
            /** Format: int32 */
            page?: number;
            /** Format: int32 */
            size?: number;
            /** Format: int64 */
            totalElements?: number;
            /** Format: int32 */
            totalPages?: number;
            sort?: string;
            hasNext?: boolean;
        };
        UnreadCountResponse: {
            /** Format: int64 */
            count?: number;
        };
        HealthStatus: {
            status?: string;
            service?: string;
            version?: string;
            /** Format: date-time */
            timestamp?: string;
        };
        DashboardResponse: {
            kpis?: components["schemas"]["Kpis"];
            stageCounts?: components["schemas"]["StageCount"][];
            statusCounts?: components["schemas"]["StatusCount"][];
            recentProjects?: components["schemas"]["ProjectSummaryResponse"][];
            myProjects?: components["schemas"]["ProjectSummaryResponse"][];
        };
        Kpis: {
            /** Format: int64 */
            activeProjects?: number;
            /** Format: int64 */
            inProduction?: number;
            /** Format: int64 */
            awaitingQa?: number;
            /** Format: int64 */
            completedThisMonth?: number;
            /** Format: int64 */
            totalProjects?: number;
        };
        StageCount: {
            stage?: string;
            /** Format: int64 */
            count?: number;
        };
        StatusCount: {
            status?: string;
            /** Format: int64 */
            count?: number;
        };
        PageResponseAuditEventResponse: {
            content?: components["schemas"]["AuditEventResponse"][];
            /** Format: int32 */
            page?: number;
            /** Format: int32 */
            size?: number;
            /** Format: int64 */
            totalElements?: number;
            /** Format: int32 */
            totalPages?: number;
            sort?: string;
            hasNext?: boolean;
        };
        PageResponseAdminUserResponse: {
            content?: components["schemas"]["AdminUserResponse"][];
            /** Format: int32 */
            page?: number;
            /** Format: int32 */
            size?: number;
            /** Format: int64 */
            totalElements?: number;
            /** Format: int32 */
            totalPages?: number;
            sort?: string;
            hasNext?: boolean;
        };
        RoleResponse: {
            /** Format: int32 */
            id?: number;
            code?: string;
            name?: string;
            description?: string;
        };
        PermissionResponse: {
            /** Format: int32 */
            id?: number;
            code?: string;
            description?: string;
        };
    };
    responses: never;
    parameters: never;
    requestBodies: never;
    headers: never;
    pathItems: never;
}
export type $defs = Record<string, never>;
export interface operations {
    progress: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                jobId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["ProgressUpdateRequest"];
            };
        };
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
        };
    };
    list: {
        parameters: {
            query: {
                stage?: string;
                imprintId?: string;
                status?: string;
                priority?: string;
                mine?: boolean;
                q?: string;
                pageable: components["schemas"]["Pageable"];
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["PageResponseProjectSummaryResponse"];
                };
            };
        };
    };
    create: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateProjectRequest"];
            };
        };
        responses: {
            /** @description Created */
            201: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ProjectResponse"];
                };
            };
        };
    };
    signOff: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                projectId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["SignoffRequest"];
            };
        };
        responses: {
            /** @description Created */
            201: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["SignoffResponse"];
                };
            };
        };
    };
    latest: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                projectId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["PreflightDetailResponse"];
                };
            };
        };
    };
    start: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                projectId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Accepted */
            202: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AiJobResponse"];
                };
            };
        };
    };
    uploadPdf: {
        parameters: {
            query?: {
                title?: string;
            };
            header?: never;
            path: {
                projectId: string;
            };
            cookie?: never;
        };
        requestBody?: {
            content: {
                "multipart/form-data": {
                    /** Format: binary */
                    file: string;
                };
            };
        };
        responses: {
            /** @description Created */
            201: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ProductionPdfResponse"];
                };
            };
        };
    };
    get: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                projectId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["PackageResponse"];
                };
            };
        };
    };
    assemble: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                projectId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Created */
            201: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["PackageResponse"];
                };
            };
        };
    };
    addItem: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                projectId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["AddPackageItemRequest"];
            };
        };
        responses: {
            /** @description Created */
            201: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["PackageResponse"];
                };
            };
        };
    };
    uploadManuscript: {
        parameters: {
            query?: {
                title?: string;
            };
            header?: never;
            path: {
                projectId: string;
            };
            cookie?: never;
        };
        requestBody?: {
            content: {
                "multipart/form-data": {
                    /** Format: binary */
                    file: string;
                };
            };
        };
        responses: {
            /** @description Created */
            201: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["DocumentResponse"];
                };
            };
        };
    };
    list_1: {
        parameters: {
            query?: {
                docType?: string;
            };
            header?: never;
            path: {
                projectId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["DocumentSummaryResponse"][];
                };
            };
        };
    };
    create_1: {
        parameters: {
            query: {
                docType: "MANUSCRIPT" | "PRODUCTION_PDF" | "STRUCTURED_XML" | "FIGURES_MANIFEST" | "OTHER";
                title?: string;
            };
            header?: never;
            path: {
                projectId: string;
            };
            cookie?: never;
        };
        requestBody?: {
            content: {
                "multipart/form-data": {
                    /** Format: binary */
                    file: string;
                };
            };
        };
        responses: {
            /** @description Created */
            201: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["DocumentResponse"];
                };
            };
        };
    };
    list_2: {
        parameters: {
            query: {
                contextType?: string;
                contextId?: string;
                pageable: components["schemas"]["Pageable"];
            };
            header?: never;
            path: {
                projectId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["PageResponseCommentResponse"];
                };
            };
        };
    };
    add: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                projectId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateCommentRequest"];
            };
        };
        responses: {
            /** @description Created */
            201: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["CommentResponse"];
                };
            };
        };
    };
    ask: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                projectId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["AssistantMessageRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AssistantMessageResponse"];
                };
            };
        };
    };
    latest_1: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                projectId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AnalysisDetailResponse"];
                };
            };
        };
    };
    start_1: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                projectId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Accepted */
            202: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AiJobResponse"];
                };
            };
        };
    };
    transition: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["TransitionRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["TransitionResponse"];
                };
            };
        };
    };
    assignMembers: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["AssignMembersRequest"];
            };
        };
        responses: {
            /** @description Created */
            201: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ProjectMemberResponse"][];
                };
            };
        };
    };
    markAllRead: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
        };
    };
    markRead: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
        };
    };
    bulkDecide: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["BulkDecisionRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["BulkDecisionResponse"];
                };
            };
        };
    };
    decide: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                issueId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["IssueDecisionRequest"];
            };
        };
        responses: {
            /** @description Created */
            201: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["IssueDecisionResponse"];
                };
            };
        };
    };
    versions: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                documentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["FileVersionResponse"][];
                };
            };
        };
    };
    addVersion: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                documentId: string;
            };
            cookie?: never;
        };
        requestBody?: {
            content: {
                "multipart/form-data": {
                    /** Format: binary */
                    file: string;
                };
            };
        };
        responses: {
            /** @description Created */
            201: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["FileVersionResponse"];
                };
            };
        };
    };
    setCurrent: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                documentId: string;
                versionId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["DocumentResponse"];
                };
            };
        };
    };
    refresh: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["RefreshTokenRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["TokenResponse"];
                };
            };
        };
    };
    logout: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["RefreshTokenRequest"];
            };
        };
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
        };
    };
    login: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["LoginRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["TokenResponse"];
                };
            };
        };
    };
    listUsers: {
        parameters: {
            query: {
                role?: string;
                status?: string;
                q?: string;
                pageable: components["schemas"]["Pageable"];
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["PageResponseAdminUserResponse"];
                };
            };
        };
    };
    create_2: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CreateUserRequest"];
            };
        };
        responses: {
            /** @description Created */
            201: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AdminUserResponse"];
                };
            };
        };
    };
    bulk: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["BulkUserRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["BulkUserResult"];
                };
            };
        };
    };
    assignRole: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["AssignRoleRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AdminUserResponse"];
                };
            };
        };
    };
    get_1: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ProjectResponse"];
                };
            };
        };
    };
    update: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["UpdateProjectRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ProjectResponse"];
                };
            };
        };
    };
    get_2: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["NotificationPreferenceResponse"][];
                };
            };
        };
    };
    update_1: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["UpdatePreferencesRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["NotificationPreferenceResponse"][];
                };
            };
        };
    };
    delete: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
        };
    };
    edit: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["UpdateCommentRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["CommentResponse"];
                };
            };
        };
    };
    get_3: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AdminUserResponse"];
                };
            };
        };
    };
    deactivate: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
        };
    };
    update_2: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["UpdateUserRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AdminUserResponse"];
                };
            };
        };
    };
    stages: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["WorkflowStageResponse"][];
                };
            };
        };
    };
    workloadByImprint: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ImprintWorkloadResponse"];
                };
            };
        };
    };
    throughput: {
        parameters: {
            query?: {
                range?: string;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ThroughputResponse"];
                };
            };
        };
    };
    overview: {
        parameters: {
            query?: {
                range?: string;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ReportOverviewResponse"];
                };
            };
        };
    };
    signoffs: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                projectId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["SignoffResponse"][];
                };
            };
        };
    };
    download: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                projectId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["StreamingResponseBody"];
                };
            };
        };
    };
    issues: {
        parameters: {
            query?: {
                severity?: string;
                status?: string;
            };
            header?: never;
            path: {
                projectId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["IssueResponse"][];
                };
            };
        };
    };
    stream: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                projectId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "text/event-stream": components["schemas"]["SseEmitter"];
                };
            };
        };
    };
    thread: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                projectId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AssistantThreadResponse"];
                };
            };
        };
    };
    approvals: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                projectId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ApprovalResponse"][];
                };
            };
        };
    };
    timeline: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["TimelineEntryResponse"][];
                };
            };
        };
    };
    activity: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AuditEventResponse"][];
                };
            };
        };
    };
    list_3: {
        parameters: {
            query: {
                unread?: boolean;
                pageable: components["schemas"]["Pageable"];
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["PageResponseNotificationResponse"];
                };
            };
        };
    };
    unreadCount: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["UnreadCountResponse"];
                };
            };
        };
    };
    list_4: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ImprintResponse"][];
                };
            };
        };
    };
    health: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["HealthStatus"];
                };
            };
        };
    };
    download_1: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                versionId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": string;
                };
            };
        };
    };
    get_4: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                documentId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["DocumentResponse"];
                };
            };
        };
    };
    get_5: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["DashboardResponse"];
                };
            };
        };
    };
    me: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["UserSummary"];
                };
            };
        };
    };
    list_5: {
        parameters: {
            query: {
                projectId?: string;
                eventType?: string;
                pageable: components["schemas"]["Pageable"];
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["PageResponseAuditEventResponse"];
                };
            };
        };
    };
    export: {
        parameters: {
            query?: {
                format?: string;
                projectId?: string;
                eventType?: string;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": string;
                };
            };
        };
    };
    get_6: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                jobId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["AiJobResponse"];
                };
            };
        };
    };
    roles: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["RoleResponse"][];
                };
            };
        };
    };
    permissions: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["PermissionResponse"][];
                };
            };
        };
    };
    removeItem: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                projectId: string;
                itemId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
        };
    };
    revokeRole: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
                roleId: number;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
        };
    };
}
