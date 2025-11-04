// Seleciona o overlay, popups e botões
const locationPopup = document.getElementById("locationPopup");
const locationPopup2 = document.getElementById("locationPopup2");
const signInBtn = document.getElementById("signInBtn"); 
const joinNowBtn = document.getElementById("joinNowBtn"); 
const closePopupBtn = document.getElementById("closePopupBtn");
const closePopupBtn2 = document.getElementById("closePopupBtn2");
const registerLink = document.getElementById("registerLink"); 

registerLink.addEventListener("click", () => {
    // Fecha o popup de registro
    locationPopup2.classList.remove("active");
    // Abre o popup de "Sign In"
    locationPopup.classList.add("active");
    overlay.classList.add("active");
});
// Exibe o popup e o overlay ao clicar no botão "Sign Up"
signInBtn.addEventListener("click", () => {
    locationPopup.classList.add("active");
    overlay.classList.add("active");
});

// Exibe o popup e o overlay ao clicar no botão "Join Now"
joinNowBtn.addEventListener("click", () => {
    locationPopup2.classList.add("active");
    overlay.classList.add("active");
});

// Oculta o popup e o overlay ao clicar no botão "Fechar"
closePopupBtn.addEventListener("click", () => {
    locationPopup.classList.remove("active");
    overlay.classList.remove("active");
});




